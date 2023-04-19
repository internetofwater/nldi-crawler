#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""
import os
import copy
import ijson
import httpx

from unittest import mock
import pytest
from pytest_httpx import HTTPXMock


@pytest.mark.order(10)
def test_list_sources(crawler_repo):
    """get table of sources from crawler_repo"""
    srcs = crawler_repo.get_list()
    assert len(srcs) == 2


@pytest.mark.order(10)
def test_find_source(crawler_repo):
    """get specific source from db"""
    _src = crawler_repo.get(12)
    assert _src is not None


@pytest.mark.order(10)
@pytest.mark.xfail(raises=ValueError)
def test_no_such_source(crawler_repo):
    """get table of sources from db"""
    _ = crawler_repo.get(999)


@pytest.mark.order(11)
def test_validate_source(crawler_repo):
    """validate source"""
    _src = crawler_repo.get(13)
    result = _src.verify()
    assert result[0] is True


@pytest.mark.order(11)
def test_validation_fail(fake_source):
    """give a URI which does not return JSON"""
    _src = copy.deepcopy(fake_source)
    _src.feature_uri = "https://www.google.com/"
    result = _src.verify()
    assert result[0] is False


@pytest.mark.order(12)
def test_validation_timeout(httpx_mock: HTTPXMock, fake_source):
    """force network timeout."""
    httpx_mock.add_exception(httpx.ReadTimeout("Unable to read within timeout"))
    result = fake_source.verify()
    assert result[0] is False


@pytest.mark.order(12)
def test_validation_json_error(fake_source):
    """force JSON error"""
    with mock.patch("ijson.items", side_effect=ijson.JSONError("ERROR")):
        result = fake_source.verify()
        assert result[0] is False


@pytest.mark.order(12)
def test_validation_name_not_found(fake_source):
    """name column not found"""
    _src = copy.deepcopy(fake_source)
    _src.feature_name = "invalid"
    result = _src.verify()
    assert result[0] is False


@pytest.mark.order(12)
def test_validation_reach_not_found(fake_source):
    """reach column not found"""
    _src = copy.deepcopy(fake_source)
    _src.feature_reach = "invalid"
    result = _src.verify()
    assert result[0] is False


@pytest.mark.order(12)
def test_validation_measure_not_found(fake_source):
    """measure column not found"""
    _src = copy.deepcopy(fake_source)
    _src.feature_measure = "invalid"
    result = _src.verify()
    assert result[0] is False


@pytest.mark.order(12)
def test_validation_uri_not_found(fake_source):
    """uri column not found"""
    _src = copy.deepcopy(fake_source)
    _src.feature_uri = "invalid"
    result = _src.verify()
    assert result[0] is False


@pytest.mark.order(13)
def test_download(fake_source):
    """download source data to local disk"""
    fname = fake_source.download_geojson()
    assert os.path.exists(fname)
    os.remove(fname)


@pytest.mark.order(13)
def test_download_network_timeout(httpx_mock: HTTPXMock, fake_source):
    """force network timeout."""
    httpx_mock.add_exception(httpx.ReadTimeout("Unable to read within timeout"))
    fname = fake_source.download_geojson()
    ## timeout is handled within download_geojson, which should return empty string in that case.
    assert fname == ""


@pytest.mark.order(13)
def test_raises_io_error(fake_source):
    """force IO error on write to file"""
    with mock.patch("tempfile.NamedTemporaryFile", side_effect=IOError("ERROR")):
        fname = fake_source.download_geojson()
        assert fname is ""
