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
from sqlalchemy.orm import DeclarativeBase, sessionmaker

class NLDI_Base(DeclarativeBase):  # pylint: disable=invalid-name,too-few-public-methods
    """Base class used to create reflected ORM objects."""


DEFAULT_DB_INFO = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5432",
    "NLDI_DB_USER": "read_only_user",
    "NLDI_DB_NAME": "nldi",
}

DEFAULT_DB_URI = URL.create(
    "postgresql+psycopg",
    username=DEFAULT_DB_INFO['NLDI_DB_USER'],
    host=DEFAULT_DB_INFO['NLDI_DB_HOST'],
    port=DEFAULT_DB_INFO['NLDI_DB_PORT'],
    database=DEFAULT_DB_INFO['NLDI_DB_NAME']
)

class DataAccessLayer:
    """
    Abstraction layer to hold connection details for the data we want to access
    via the DB connection
    """
    def __init__(self, uri=DEFAULT_DB_URI):
        self.engine = None
        self.session = None
        self.uri = uri

    def connect(self):
        """
        opens a connection to the database, returning a Session
        """
        if self.engine is None:
            self.engine = create_engine(self.uri, client_encoding="UTF-8", echo=False, future=True)
        else:
            logging.warning("Attempt to re-open an already-open connection.")
        self.Session = sessionmaker(bind=self.engine) # pytlint: disable=invalid-name,attribute-defined-outside-init

    def disconnect(self):
        """
        closes the open engine
        """
        if self.engine is None:
            logging.warning("Attempt to close a connection that isn't open.")
        else:
            self.engine = None
