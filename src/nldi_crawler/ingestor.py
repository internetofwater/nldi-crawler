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

import ijson
from sqlalchemy.sql import text
from sqlalchemy.exc import SQLAlchemyError

from sqlalchemy import Integer, Numeric, Table, Column, MetaData
from sqlalchemy.orm import registry


from shapely import from_geojson, to_wkt
from geoalchemy2.elements import WKTElement
from geoalchemy2 import Geometry

from .source import CrawlerSource
from .feature import CrawledFeature
from .db import StrippedString, DataAccessLayer


### EPSG codes for coordinate reference systems we might use.
_NAD_83 = 4269
_WGS_84 = 4326
###
DEFAULT_SRS = _NAD_83


def sql_ingestor(src: CrawlerSource, dal: DataAccessLayer) -> int:
    """
    Takes in a source object and processes it to insert into the NLDI-DB feature table.

    The source object is expected to implement a feature_list() method that returns a list of
    features to be ingested.  The source object is also expected to implement a tablename() method
    that returns the name of the table to be used for the ingest.

    :param src: The source to be ingested
    :type src: CrawlerSource()
    """
    logging.info(
        " Ingesting from %s source: %s / %s",
        src.ingest_type.upper(),
        src.crawler_source_id,
        src.source_name,
    )

    ## This is where the ORM magic happens. We create a temporary table that is a clone of the
    ## feature table, and then we map the CrawledFeature class to that table.  We can then use
    ## the ORM to insert the data into the table.
    tmp = src.tablename("tmp")
    _metadata = MetaData()
    tmp_feature_table = Table(
        tmp,
        _metadata,
        Column("comid", Integer),
        Column("identifier", StrippedString, primary_key=True),
        Column("crawler_source_id", Integer, primary_key=True),
        Column("name", StrippedString),
        Column("uri", StrippedString),
        Column("reachcode", StrippedString),
        Column("measure", Numeric(precision=38, scale=10)),
        Column("location", Geometry("POINT", srid=DEFAULT_SRS)),
        schema="nldi_data",
    )
    mapper_registry = registry()
    mapper_registry.map_imperatively(
        CrawledFeature,
        tmp_feature_table,
    )

    i = 1
    try:
        dal.connect()
        with dal.Session() as session:
            for itm in src.feature_list(stream=False):
                i += 1
                shp = from_geojson(json.dumps(itm["geometry"]))
                elmnt = WKTElement(to_wkt(shp), srid=DEFAULT_SRS)
                logging.debug("%s : %s", itm["properties"][src.feature_name], to_wkt(shp))
                try:
                    meas = float(itm["properties"][src.feature_measure])
                except (ValueError, NameError, KeyError, TypeError):
                    meas = 0.0
                try:
                    _my_id = itm["id"]
                except KeyError:
                    _my_id = itm["properties"][src.feature_id]

                _feature = CrawledFeature(
                    comid=0,
                    identifier=_my_id,
                    crawler_source_id=src.crawler_source_id,
                    name=itm["properties"][src.feature_name],
                    uri=itm["properties"][src.feature_uri],
                    location=elmnt,
                    reachcode=itm["properties"][src.feature_reach],
                    measure=meas,
                )
                session.add(_feature)
                session.commit()
        logging.info(" Processed %s features from %s", i - 1, src.source_name)
    except ijson.JSONError:
        logging.warning(" Parsing error; stopping after %s features read", i - 1)
    except SQLAlchemyError:
        logging.exception(" Database session error. Stopping", exc_info=True)
    finally:
        mapper_registry.dispose()
        dal.disconnect()
    return i - 1


def create_tmp_table(dal: DataAccessLayer, src: CrawlerSource) -> None:
    """
    This method of creating the temp table relies completely on the postgress dialect of SQL to
    do the work. We could use sqlalchemy mechanisms to achieve something similar, but this is
    quick and easy -- and it eliminates some problems if the created table is not truly identical
    to the `features` table it models on. This will become important when we establish inheritance
    among tables later.
    """
    tmp = src.tablename("tmp")
    dal.connect()
    stmt = f"""
        DROP TABLE IF EXISTS nldi_data.{tmp};
        CREATE TABLE IF NOT EXISTS nldi_data.{tmp}
            (LIKE nldi_data.feature INCLUDING INDEXES);
    """
    with dal.Session() as session:
        session.execute(text(stmt))
        session.commit()
    dal.disconnect()


def install_data(dal: DataAccessLayer, src: CrawlerSource) -> None:
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
    old = src.tablename("old")
    tmp = src.tablename("tmp")
    table = src.tablename()
    schema = "nldi_data"

    dal.connect()
    stmt = f"""
        set search_path = {schema};
        DROP TABLE IF EXISTS {old};
        ALTER TABLE IF EXISTS {table} NO INHERIT feature;
        ALTER TABLE IF EXISTS {table} RENAME TO {old};
        ALTER TABLE {tmp} RENAME TO {table};
        ALTER TABLE {table} INHERIT feature;
        DROP TABLE IF EXISTS {old}
    """
    with dal.Session() as session:
        session.execute(text(stmt))
        session.commit()
    dal.disconnect()
