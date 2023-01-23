#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""
import pytest
from unittest import mock
from sqlalchemy.exc import SQLAlchemyError

from nldi_crawler import source


def test_list_sources(dal):
    """get table of sources from db"""
    srcs = source.list_sources(dal)
    assert len(srcs) >= 1


def test_find_source(dal):
    """get specific source from db"""
    srcs = source.list_sources(dal, selector=10)
    assert len(srcs) == 1


def test_no_such_source(dal):
    """get table of sources from db"""
    srcs = source.list_sources(dal, selector=100)
    assert len(srcs) == 0


@pytest.mark.xfail(raises=SQLAlchemyError)
def test_failed_db_session(dal):
    """force exceptions"""
    with mock.patch.object(dal, "Session", side_effect=SQLAlchemyError("Error")):
        srcs = source.list_sources(dal)


def test_source_attributes(dal):
    """tests getters for extended attributes"""
    src = source.list_sources(dal, selector=13)[0]
    assert src.crawler_source_id == 13
    assert src.source_suffix == "geoconnex-demo"
    assert src.table_name() == "feature_geoconnex_demo"
    assert src.table_name("tmp") == "feature_geoconnex_demo_tmp"
