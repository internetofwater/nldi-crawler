#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test database ingestor.
"""
from unittest import mock
import pytest

from ijson import JSONError
from sqlalchemy.exc import SQLAlchemyError

from nldi_crawler import ingestor


@pytest.mark.order(30)
@pytest.mark.integration
def test_ingest_test_file_json_error(dal, fake_source):
    """read and ingest from test file"""
    with mock.patch("ijson.items", side_effect=JSONError("Error")):
        i = ingestor.sql_ingestor(fake_source, dal)
        assert i == 0


@pytest.mark.order(30)
@pytest.mark.integration
def test_ingest_test_file_sql_error(dal, fake_source):
    """read and ingest from test file -- induce SQL error to abort the ingest"""
    ingestor.create_tmp_table(dal, fake_source)
    with mock.patch.object(dal, "Session", side_effect=SQLAlchemyError("Error")):
        i = ingestor.sql_ingestor(fake_source, dal)
        assert i == 0


# def test_ingest_test_file(dal, dummy_source):
#     """read and ingest from test file"""
#     ingestor.create_tmp_table(dal, dummy_source)
#     thisdir = os.path.dirname(__file__)
#     i = ingestor.sql_ingestor(dummy_source, dal)
#     assert i == 1
