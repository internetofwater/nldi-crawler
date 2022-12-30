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

import sqlalchemy
import geoalchemy2
from sqlalchemy.orm import DeclarativeBase


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

    engine = sqlalchemy.create_engine(db_url, client_encoding="UTF-8", echo=True, future=True)
    meta = sqlalchemy.MetaData()

    feature_test = sqlalchemy.Table( # pylint: disable=W0612
        tablename,
        meta,
        sqlalchemy.Column("comid", sqlalchemy.Integer, primary_key=True, autoincrement=False),
        sqlalchemy.Column("crawler_source_id", sqlalchemy.Integer),
        sqlalchemy.Column("name", sqlalchemy.String),
        sqlalchemy.Column("uri", sqlalchemy.String),
        sqlalchemy.Column("location", sqlalchemy.String),
        sqlalchemy.Column("reachcode", sqlalchemy.String),
        sqlalchemy.Column("measure", sqlalchemy.Numeric),
        sqlalchemy.Column("shape", geoalchemy2.Geography(geom_type)),
        schema="nldi_data",
    )
    meta.create_all(engine)
