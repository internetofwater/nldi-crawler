#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
classes and functions surrounding database access
"""
from typing import Any, Optional, Type

import logging

from sqlalchemy import create_engine
from sqlalchemy.engine import URL
import sqlalchemy.types
from sqlalchemy.orm import Session

from nldi_crawler.config import DEFAULT_DB_INFO

DEFAULT_DB_URI = URL.create(
    "postgresql+psycopg2",
    username=str(DEFAULT_DB_INFO["NLDI_DB_USER"]),
    host=str(DEFAULT_DB_INFO["NLDI_DB_HOST"]),
    port=int(DEFAULT_DB_INFO["NLDI_DB_PORT"]),
    database=str(DEFAULT_DB_INFO["NLDI_DB_NAME"]),
)


class DataAccessLayer:
    """
    Abstraction layer to hold connection details for the data we want to access
    via the DB connection
    """

    def __init__(self, uri: URL = DEFAULT_DB_URI):
        self.engine = None
        self.session = None
        self.uri = uri

    def connect(self) -> None:
        """
        opens a connection to the database. Sets self.engine as the way to access that connection.
        """
        if self.engine is None:
            self.engine = create_engine(self.uri, client_encoding="UTF-8", echo=False, future=True)
        else:
            logging.warning("Attempt to re-open an already-open connection.")

    def disconnect(self) -> None:
        """
        closes the open engine
        """
        if self.engine is None:
            logging.warning("Attempt to close a connection that isn't open.")
        else:
            self.engine.dispose()
            self.engine = None

    def Session(self) -> Session:  # pylint: disable=invalid-name
        """
        Opens a sqlalchemy.orm.Session() using the engine defined at instatiation time.
        """
        if self.engine is None:
            logging.warning("Cannot open a session without connection. Calling connect() for you.")
            self.connect()
        return Session(self.engine)

    def __enter__(self) -> "DataAccessLayer":
        self.connect()
        return self

    def __exit__(
        self,
        exc_type: Optional[Type[BaseException]],
        exc_value: Optional[BaseException],
        traceback: Optional[Any],
    ) -> None:
        self.disconnect()


class StrippedString(sqlalchemy.types.TypeDecorator):  # pylint: disable=too-many-ancestors
    """
    Custom type to extend String.  We use this to forcefully remove any non-printing characters
    from the input string. Some non-printables (including backspace and delete), if included
    in the String, can mess with the SQL submitted by the connection engine.
    """

    impl = sqlalchemy.types.String  ## SQLAlchemy wants it this way instead of subclassing String
    cache_ok = True

    def process_bind_param(self, value, _):
        if value is None:
            return ""
        return value.encode("ascii", errors="replace").decode("utf-8")
