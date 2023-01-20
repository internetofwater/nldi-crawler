#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""

import os
import copy
from nldi_crawler import source


def test_download(dummy_source):
    """download source data"""
    fname = source.download_geojson(dummy_source)
    assert os.path.exists(fname)
    os.remove(fname)


def test_validation_fail(dummy_source):
    """give a URI which does not return JSON"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_uri = "https://www.google.com/"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_name_not_found(dummy_source):
    _s = copy.deepcopy(dummy_source)
    _s.feature_name = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False

def test_validation_reach_not_found(dummy_source):
    _s = copy.deepcopy(dummy_source)
    _s.feature_reach = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False

def test_validation_measure_not_found(dummy_source):
    _s = copy.deepcopy(dummy_source)
    _s.feature_measure = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False

def test_validation_uri_not_found(dummy_source):
    _s = copy.deepcopy(dummy_source)
    _s.feature_uri = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False

def test_validation_success(dummy_source):
    result = source.validate_src(dummy_source)
    assert result[0] is True
