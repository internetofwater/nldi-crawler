#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test suite configuration file.
"""
import pytest
from sqlalchemy.engine import URL
from nldi_crawler.db import DataAccessLayer

from nldi_crawler import source


@pytest.fixture
def db_uri():
    """Default database connection string"""
    return URL.create(
        "postgresql+psycopg2",
        username="nldi_schema_owner",
        password="changeMe",
        host="172.18.0.1",
        port=5432,
        database="nldi",
    )


@pytest.fixture
def dal(db_uri):
    """Data Access Layer fixture to ease connections"""
    _dal = DataAccessLayer(db_uri)
    yield _dal
    _dal.disconnect()


@pytest.fixture
def crawler_repo():
    """in-memory table of crawler sources, faking an external source"""
    return source.FakeSrcRepo()


@pytest.fixture
def fake_source(crawler_repo):
    """pretend source to use instead of connecting to a database"""
    return crawler_repo.get(102)
