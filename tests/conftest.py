#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test suite configuration file.
"""
import pytest
from sqlalchemy.engine import URL
from nldi_crawler.db import DataAccessLayer

# import sqlalchemy


@pytest.fixture(scope="session")
def dal():
    """Data Access Layer fixture to ease connections"""
    url = URL.create(
        "postgresql+psycopg",
        username="nldi_schema_owner",
        password="changeMe",
        host="172.18.0.1",
        port="5432",
        database="nldi"
    )
    _dal = DataAccessLayer(url)
    yield _dal
    _dal.disconnect()
