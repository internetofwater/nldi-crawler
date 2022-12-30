#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""
import os
import pytest

from nldi_crawler import source


def test_list_sources(connect_string):
    """get table of sources from db"""
    srcs = source.fetch_source_table(connect_string)
    assert len(srcs) >= 1


def test_find_source(connect_string):
    """get specific source from db"""
    srcs = source.fetch_source_table(connect_string, selector="10")
    assert len(srcs) == 1


def test_no_such_source(connect_string):
    """get table of sources from db"""
    srcs = source.fetch_source_table(connect_string, selector="00")
    assert len(srcs) == 0


def test_download_source(connect_string):
    """get table of sources from db"""
    srcs = source.fetch_source_table(connect_string, selector="10")
    fname = source.download_geojson(srcs[-1])
    assert os.path.exists(fname)
    os.remove(fname)


def test_source_properties(connect_string):
    """property methods"""
    src = source.fetch_source_table(connect_string, selector="10")[0]
    assert src.source_suffix == "vigil"
    assert src.table_name() == "feature_vigil"
    assert src.table_name("temp") == "feature_vigil_temp"
    assert src.table_name("old") == "feature_vigil_old"


def test_failed_db_connection(connect_string):
    """Failed db connection raises ConnectionError"""
    _uri = connect_string.replace("changeMe", "noPass")
    with pytest.raises(ConnectionError):
        _ = source.fetch_source_table(_uri)


def test_validate_single_source_fail(connect_string):
    src = source.fetch_source_table(connect_string, selector="1")[0]
    # source ID=1 is known to timeout and fail validation
    result = source.validate_src(src)
    assert result[0] == False
