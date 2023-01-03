#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the table of crawler_sources
"""
import dataclasses
import logging

from sqlalchemy import Integer, String, Numeric, Column, Table, create_engine, MetaData
from sqlalchemy.orm import DeclarativeBase
from geoalchemy2 import Geography
from sqlalchemy.exc import OperationalError, DataError


@dataclasses.dataclass
class NLDI_Base(DeclarativeBase):  # pylint: disable=invalid-name
    """Base class used to create reflected ORM objects."""


def init_feature_table(db_url: str, tablename, geom_type="POINT"):
    """
    Create an empty temporary table to hold ingested features.

    :param db_url: connection string to database
    :type db_url: str
    :param tablename: The name of the table to create
    :type tablename: str
    :param geom_type: the type of geometry to associate with the 'shape' column, defaults to "POINT"
    :type geom_type: str, optional
    """
    logging.info("Creating empty feature table %s", tablename)
    try:
        engine = create_engine(db_url, client_encoding="UTF-8", echo=True, future=True)
        meta = MetaData()

        feature_test = Table(  # pylint: disable=W0612
            tablename,
            meta,
            Column("comid", Integer, primary_key=True, autoincrement=False),
            Column("crawler_source_id", Integer),
            Column("name", String),
            Column("uri", String),
            Column("location", String),
            Column("reachcode", String),
            Column("measure", Numeric(precision=38, scale=10)),
            Column("shape", Geography(geom_type)),
            schema="nldi_data",
        )
        meta.create_all(engine)
    except (OperationalError, DataError):
        logging.error("Unable to create table", exc_info=True)
        raise OSError
