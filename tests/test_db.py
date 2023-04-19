#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""

import pytest
from sqlalchemy import text
from sqlalchemy.engine import URL
from nldi_crawler.db import DataAccessLayer



@pytest.mark.order(20)
@pytest.mark.integration
@pytest.mark.xfail
def test_failed_db_connection():
    """Failed db connection raises SQLAlchemyError"""
    url = URL.create(
        "postgresql+psycopg",
        username="nldi_schema_owner",
        password="invalid",  ##<<< this will cause password authentication fail
        host="172.18.0.1",
        port=5432,
        database="nldi",
    )
    dal = DataAccessLayer(url)
    dal.connect()
    with dal.Session() as session:
        _ = session.execute(text("SELECT crawler_source_id FROM nldi_data.crawler_source"))
    assert True  ## if we get to here, then this did not XFAIL.  Problem.


@pytest.mark.order(20)
@pytest.mark.integration
def test_dal(dal):
    """
    Exercises the data access layer connection.
    Note that this requires a valid database up and running for us to connect to.
    """
    dal.connect()
    dal.connect()  ##<<< should log a message, but not an error.
    dal.disconnect()
    s = (
        dal.Session()
    )  ##<< creating a session without a connection should log a message, but not error.
    assert s is not None
