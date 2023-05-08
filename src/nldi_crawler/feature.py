#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Defines Feature model
"""
import sqlalchemy.types
from sqlalchemy import Integer, Numeric
from sqlalchemy.orm import mapped_column
from geoalchemy2 import Geometry
from pydantic.dataclasses import dataclass
from typing import Optional

from .db import NLDI_Base

### EPSG codes for coordinate reference systems we might use.
_NAD_83 = 4269
_WGS_84 = 4326
###
DEFAULT_SRS = _NAD_83


@dataclass
class CrawledFeature:
    """
    Defines the Feature model
    """

    comid: Optional[int]
    identifier: Optional[str]
    crawler_source_id: Optional[int]
    name: Optional[str]
    uri: Optional[str]
    reachcode: Optional[str]
    measure: Optional[float]
    location: Optional[str]


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


class NLDI_Feature(NLDI_Base):  # pylint: disable=invalid-name
    """
    ORM mapping object to connect with the nldi_data.feature table

    Note that the table name and the schema are defined in the class attributes, and are
    not set up to be configurable at runtime.

    TODO: make these configurable at runtime.

    """

    __tablename__ = "features"
    __table_args__ = {"schema": "nldi_data", "keep_existing": True}
    comid = mapped_column(Integer)
    identifier = mapped_column(StrippedString, primary_key=True)
    crawler_source_id = mapped_column(Integer, primary_key=True)
    name = mapped_column(StrippedString)
    uri = mapped_column(StrippedString)
    reachcode = mapped_column(StrippedString)
    measure = mapped_column(Numeric(precision=38, scale=10))
    location = mapped_column(Geometry("POINT", srid=DEFAULT_SRS))
