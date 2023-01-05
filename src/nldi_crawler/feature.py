#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the table of crawler_sources
"""
# import dataclasses
import logging

from sqlalchemy import Integer, String, Numeric, Column, Table, create_engine, MetaData

# from sqlalchemy.orm import DeclarativeBase
from geoalchemy2 import Geometry
from sqlalchemy.exc import SQLAlchemyError

_NAD_83 = 4269
_WGS_84 = 4326
DEFAULT_SRS = _WGS_84

# @dataclasses.dataclass
# class NLDI_Base(DeclarativeBase):  # pylint: disable=invalid-name
#     """Base class used to create reflected ORM objects."""


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
            # wew don't need feature_test; but we do need to instantiate a Table for its side
            # effects on meta.
            tablename,
            meta,
            Column("comid", Integer, primary_key=True, autoincrement=False),
            Column("crawler_source_id", Integer),
            Column("name", String),
            Column("uri", String),
            Column("location", String),
            Column("reachcode", String),
            Column("measure", Numeric(precision=38, scale=10)),
            Column("shape", Geometry(geom_type, srid=DEFAULT_SRS)),
            schema="nldi_data",
        )
        meta.create_all(engine)
    except SQLAlchemyError:
        logging.error("Unable to create table", exc_info=True)
        raise
