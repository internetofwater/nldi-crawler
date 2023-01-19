#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""

from nldi_crawler import source

def test_list_sources(dal):
    """get table of sources from db"""
    srcs = source.list_sources(dal)
    assert len(srcs) >= 1

def test_single_source(dal):
    src = source.list_sources(dal, selector=13)[0]
    assert src.crawler_source_id == 13
    assert src.source_suffix == "geoconnex-demo"
    assert src.table_name() == "feature_geoconnex_demo"
    assert src.table_name("tmp") == "feature_geoconnex_demo_tmp"