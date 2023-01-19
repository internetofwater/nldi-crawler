#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the ingestion of crawler sources
"""
import logging
import json

from ijson import JSONError, items
import sqlalchemy.types
from sqlalchemy import create_engine, Integer, Numeric
from sqlalchemy.orm import Session, mapped_column
from sqlalchemy.sql import text


from shapely import from_geojson, to_wkt
from geoalchemy2.elements import WKTElement
from geoalchemy2 import Geometry

from .db import NLDI_Base
from .source import CrawlerSource

### EPSG codes for coordinate reference systems we might use.
_NAD_83 = 4269
_WGS_84 = 4326
###
DEFAULT_SRS = _NAD_83


class StrippedString(sqlalchemy.types.TypeDecorator):
    """
    Custom type to extend String.  We use this to forcefully remove any non-printing characters
    from the input string. Some non-printables (including backspace and delete), if included
    in the String, can mess with the SQL submitted by the connection engine.
    """

    impl = sqlalchemy.types.String  ## SQLAlchemy wants it this way instead of subclassing String
    cache_ok = True

    def process_bind_param(self, value, dialect):
        if value is None:
            return ""
        return value.encode("ascii", errors="replace").decode("utf-8")


def ingest_from_file(src, fname: str, connect_string: str):
    """
    Takes in a source dataset, and processes it to insert into the NLDI-DB feature table

    :param src: The source to be ingested
    :type src: CrawlerSource()
    :param fname: The name of the local copy of the dataset.
    :type fname: str
    """
    tmp = src.table_name("tmp")

    class NLDI_Feature(NLDI_Base):
        __tablename__ = tmp
        __table_args__ = {"schema": "nldi_data"}
        comid = mapped_column(Integer)
        identifier = mapped_column(StrippedString, primary_key=True)
        crawler_source_id = mapped_column(Integer, primary_key=True)
        name = mapped_column(StrippedString)
        uri = mapped_column(StrippedString)
        reachcode = mapped_column(StrippedString)
        measure = mapped_column(Numeric(precision=38, scale=10))
        location = mapped_column(Geometry("POINT", srid=DEFAULT_SRS))

    logging.info(
        " Ingesting from %s source: %s / %s",
        src.ingest_type.upper(),
        src.crawler_source_id,
        src.source_name,
    )
    _id = src.feature_id
    _name = src.feature_name
    _reachcode = src.feature_reach
    _reachmeas = src.feature_measure
    _uri = src.feature_uri

    eng = create_engine(connect_string, client_encoding="UTF-8", echo=False, future=True)
    try:
        i = 1
        with open(fname, "r", encoding="UTF-8") as read_fh:
            with Session(eng) as session:
                for itm in items(read_fh, "features.item", use_float=True):
                    i += 1
                    shp = from_geojson(json.dumps(itm["geometry"]))
                    elmnt = WKTElement(to_wkt(shp), srid=DEFAULT_SRS)
                    logging.debug("%s : %s", itm["properties"][_name], to_wkt(shp))
                    try:
                        m = float(itm["properties"][_reachmeas])
                    except (ValueError, NameError, KeyError, TypeError):
                        m = 0.0
                    try:
                        _my_id = itm["id"]
                    except KeyError:
                        _my_id = itm["properties"][_id]

                    f = NLDI_Feature(
                        identifier=_my_id,
                        crawler_source_id=src.crawler_source_id,
                        name=itm["properties"][_name],
                        uri=itm["properties"][_uri],
                        location=elmnt,
                        reachcode=itm["properties"][_reachcode],
                        measure=m,
                    )
                    session.add(f)
                    session.commit()
        logging.info(" Processed %s features from %s", i - 1, src.source_name)
    except JSONError:
        logging.warning(" Parsing error; stopping after %s features read", i - 1)


def create_tmp_table(connect_string: str, src: CrawlerSource):
    """
    This method of creating the temp table relies completely on the postgress dialect of SQL to
    do the work. We could use sqlalchemy mechanisms to achieve something similar, but this is
    quick and easy -- and it eliminates some problems if the created table is not truly identical
    to the `features` table it models on. This will become important when we establish inheritance
    among tables later.
    """
    tmp = src.table_name("tmp")
    eng = create_engine(connect_string, client_encoding="UTF-8", echo=False, future=True)
    stmt = f"""
        DROP TABLE IF EXISTS nldi_data.{tmp};
        CREATE TABLE IF NOT EXISTS nldi_data.{tmp}
            (LIKE nldi_data.feature INCLUDING INDEXES);
    """
    with Session(eng) as session:
        session.execute(text(stmt))
        session.commit()
    eng.dispose()


def install_data(connect_string: str, src: CrawlerSource):
    """
    To 'install' the ingested data, we will manipulate table names and inheritance.

    The data design has the various sources (named 'feature_{suffix}') INHERIT from
    the `feature` parent table. Queries against `feature` will return rows from any
    child that inherits from it.

    The workflow here is to take the already-populated feature_{suffix}_tmp table
    and shuffle the table names:
      * remove feature_{suffix}_old
      * Remove inheritance relationship between feature and feature_{suffix}
      * rename feature_{suffix} to feature_{suffix}_old
      * rename feature_{suffix}_tmp to feature_{suffix}
      * re-establish inheritance between feature and feature_{suffix}
      * remove the feature_{suffix}_old table
    """
    old = src.table_name("old")
    tmp = src.table_name("tmp")
    table = src.table_name()
    schema = "nldi_data"

    eng = create_engine(connect_string, client_encoding="UTF-8", echo=True, future=True)
    stmt = f"""
        set search_path = {schema};
        DROP TABLE IF EXISTS {old};
        ALTER TABLE IF EXISTS {table} NO INHERIT feature;
        ALTER TABLE IF EXISTS {table} RENAME TO {old};
        ALTER TABLE {tmp} RENAME TO {table};
        ALTER TABLE {table} INHERIT feature;
        DROP TABLE IF EXISTS {old}
    """
    with Session(eng) as session:
        session.execute(text(stmt))
        session.commit()
    eng.dispose()
