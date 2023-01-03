#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the ingestion of crawler sources
"""
import logging
from ijson import JSONError, items
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from .source import CrawlerSource

_NAD_83 = 4269
_WGS_84 = 4326
DEFAULT_SRS = _WGS_84


def ingest_from_file(src, fname: str):
    """
    Takes in a source dataset, and processes it to insert into the NLDI-DB feature table

    :param src: The source to be ingested
    :type src: CrawlerSource()
    :param fname: The name of the local copy of the dataset.
    :type fname: str
    """
    logging.info(
        " Ingesting from %s source: %s / %s",
        src.ingest_type.upper(),
        src.crawler_source_id,
        src.source_name,
    )
    try:
        i = 1
        with open(fname, "r", encoding="UTF-8") as read_fh:
            for itm in items(read_fh, "features.item"):
                i += 1
        logging.info(" Processed %s features from %s", i - 1, src.source_name)
    except JSONError:
        logging.warning(" Parsing error; stopping after %s features read", i - 1)



def create_tmp_table(connect_str:str, src:CrawlerSource):
    """
    This method of creating the temp table relies completely on the postgress dialect of SQL to
    do the work. We could use sqlalchemy mechanisms to achieve something similar, but this is
    quick and easy -- and it eliminates some problems if the created table is not truly identical
    to the `features` table it models on. This will become important when we establish inheritance
    among tables later.
    """
    tmp = src.table_name("tmp")
    stmt=f"""
        DROP TABLE IF EXISTS nldi_data.{tmp};
        CREATE TABLE IF NOT EXISTS nldi_data.{tmp}
            (LIKE nldi_data.feature INCLUDING INDEXES);
    """


def install_data(connect_string:str, src:CrawlerSource):
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

    eng = create_engine(connect_string, client_encoding="UTF-8", echo=False, future=True)
    stmt = f"""
        DROP TABLE IF EXISTS {old};
        ALTER TABLE IF EXISTS {table} NO INHERIT feature;
        ALTER TABLE {tmp} IF EXISTS {table} RENAME TO {old};
        ALTER TABLE {tmp} RENAME TO {table};
        ALTER TABLE {table} INHERIT feature;
        DROP TABLE IF EXISTS {old}
    """
    with Session(eng) as session:
        session.execute(stmt)
