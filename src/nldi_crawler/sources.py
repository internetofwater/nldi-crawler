#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the table of crawler_sources
"""
import os
import sys
import dataclasses
import tempfile
import logging
import httpx

from sqlalchemy import create_engine, Table, select
from sqlalchemy.orm import DeclarativeBase, Session, mapped_column


@dataclasses.dataclass
class NldiBase(DeclarativeBase):
    """Base class used to create reflected ORM objects."""


def fetch_source_table(connect_string: str, selector="") -> list:
    """
    Fetches a list of crawler sources from the master NLDI-DB database.  The returned list
    holds one or mor CrawlerSource() objects, which are reflected from the database using
    the sqlalchemy ORM.

    :param connect_string: The db URL used to connect to the database
    :type connect_string: str
    :return: A list of sources
    :rtype: list of CrawlerSource objects
    """
    _tbl_name_ = "crawler_source"
    _schema_ = "nldi_data"

    eng = create_engine(connect_string, client_encoding="UTF-8", echo=False, future=True)
    retval = []

    @dataclasses.dataclass
    class CrawlerSource(NldiBase):
        """
        An ORM reflection of the crawler_source table
        """
        __tablename__ = "crawler_source"
        __table_args__ = {"schema": "nldi_data"}

        crawler_source_id = mapped_column(Integer, primary_key=True)
        source_name = mapped_column(String(64))
        source_suffix = mapped_column(String(16))
        source_uri = mapped_column(String)
        feature_id = mapped_column(String)
        feature_name = mapped_column(String)
        feature_uri = mapped_column(String)
        feature_reach = mapped_column(String)
        feature_measure = mapped_column(String)
        ingest_type = mapped_column(String(16))
        feature_type = mapped_column(String)
        # __table__ = Table(
        #     _tbl_name_,  ## <--- name of the table
        #     NldiBase.metadata,
        #     autoload_with=eng,  ## <--- this is where the magic happens
        #     schema=_schema_,  ## <--- only need this if the table is not in the default schema.
        # )
        @property
        def table_name(self):
            return "feature_" + self.source_suffix

        @property
        def tmp_table_name(self):
            return self.table_name + "_temp"

        @property
        def old_table_name(self):
            return self.table_name + "_old"




    if selector == "":
        stmt = select(CrawlerSource).order_by(# pylint: disable=E1101
            CrawlerSource.crawler_source_id                    # pylint: disable=E1101
        )
    else:
        stmt = (
            select(CrawlerSource)
            .where(CrawlerSource.crawler_source_id == selector)# pylint: disable=E1101
            .order_by(CrawlerSource.crawler_source_id)         # pylint: disable=E1101
        )

    with Session(eng) as session:
        for source in session.scalars(stmt):
            retval.append(source)
    eng = None
    return retval



def download_geojson(source) -> str:
    """
    Downloads data from the specified source, saving it to a temporary file on local disk.

    :param source: The descriptor for the source.
    :type source: CrawlerSource()
    :return: path name to temporary file
    :rtype: str
    """
    logging.info("Downloading from %s ...", source.source_uri)
    fname="_tmp"
    try:
        with tempfile.NamedTemporaryFile(
            suffix=".geojson",
            prefix=f"CrawlerData_{source.crawler_source_id}_",
            dir=".",
            delete=False,
        ) as tmp_fh:
            fname = tmp_fh.name
            logging.info("Writing to tmp file %s", tmp_fh.name)
            # timeout = 15sec  TODO: make this a tunable
            with httpx.stream("GET", source.source_uri, timeout=15.0, follow_redirects=True) as response:
                for chunk in response.iter_bytes():
                    tmp_fh.write(chunk)
    except IOError:
        logging.exception("I/O Error while downloading from %s to %s", source.source_uri, fname)
        raise
    except httpx.ReadTimeout:
        logging.critical("Read TimeOut attempting to download from %s", source.source_uri)
        os.remove(fname)
        return None
    return fname
