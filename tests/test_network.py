#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""

import os
import copy
import pytest
from unittest import mock

import ijson
import httpx
from pytest_httpx import HTTPXMock
from nldi_crawler import source


def test_validation_name_not_found(dummy_source):
    """name column not found"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_name = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_reach_not_found(dummy_source):
    """reach column not found"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_reach = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_measure_not_found(dummy_source):
    """measure column not found"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_measure = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_uri_not_found(dummy_source):
    """uri column not found"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_uri = "invalid"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_fail(dummy_source):
    """give a URI which does not return JSON"""
    _s = copy.deepcopy(dummy_source)
    _s.feature_uri = "https://www.google.com/"
    result = source.validate_src(_s)
    assert result[0] is False


def test_validation_timeout(httpx_mock: HTTPXMock, dummy_source):
    """force network timeout."""
    httpx_mock.add_exception(httpx.ReadTimeout("Unable to read within timeout"))
    result = source.validate_src(dummy_source)
    assert result[0] is False


def test_validation_json_error(dummy_source):
    """force JSON error"""
    with mock.patch("ijson.items", side_effect=ijson.JSONError("ERROR")):
        result = source.validate_src(dummy_source)
        assert result[0] is False


def test_validation_success(dummy_source):
    """finally, one that works."""
    result = source.validate_src(dummy_source)
    assert result[0] is True


def test_download(dummy_source):
    """download source data to local disk"""
    fname = source.download_geojson(dummy_source)
    assert os.path.exists(fname)
    os.remove(fname)


def test_download_network_timeout(httpx_mock: HTTPXMock, dummy_source):
    """force network timeout."""
    httpx_mock.add_exception(httpx.ReadTimeout("Unable to read within timeout"))
    fname = source.download_geojson(dummy_source)
    ## timeout is handled within download_geojson, which should return None in that case.
    assert fname is None


def test_raises_io_error(dummy_source):
    """force IO error on write to file"""
    with mock.patch("tempfile.NamedTemporaryFile", side_effect=IOError("ERROR")):
        fname = source.download_geojson(dummy_source)
        assert fname is None
