#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
classes and functions surrounding database access
"""
import logging

from sqlalchemy import create_engine
from sqlalchemy.engine import URL
from sqlalchemy.orm import DeclarativeBase, Session


DEFAULT_DB_INFO = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5432",
    "NLDI_DB_USER": "read_only_user",
    "NLDI_DB_NAME": "nldi",
}


class NLDI_Base(DeclarativeBase):  # pylint: disable=invalid-name,too-few-public-methods
    """Base class used to create reflected ORM objects."""


class DataAccessLayer:
    """
    Abstraction layer to hold connection details for the data we want to access
    via the DB connection
    """

    DEFAULT_DB_URI = URL.create(
        "postgresql+psycopg",
        username=DEFAULT_DB_INFO["NLDI_DB_USER"],
        host=DEFAULT_DB_INFO["NLDI_DB_HOST"],
        port=DEFAULT_DB_INFO["NLDI_DB_PORT"],
        database=DEFAULT_DB_INFO["NLDI_DB_NAME"],
    )

    def __init__(self, uri=DEFAULT_DB_URI):
        self.engine = None
        self.session = None
        self.uri = uri

    def connect(self):
        """
        opens a connection to the database. Sets self.engine as the way to access that connection.
        """
        if self.engine is None:
            self.engine = create_engine(self.uri, client_encoding="UTF-8", echo=False, future=True)
        else:
            logging.warning("Attempt to re-open an already-open connection.")

    def disconnect(self):
        """
        closes the open engine
        """
        if self.engine is None:
            logging.warning("Attempt to close a connection that isn't open.")
        else:
            self.engine = None

    def Session(self):  # pytlint: disable=invalid-name
        """
        Opens a sqlalchemy.orm.Session() using the engine defined at instatiation time.
        """
        if self.engine is None:
            logging.warning("Cannot open a session without connection. Calling connect() for you.")
            self.connect()
        return Session(self.engine)
