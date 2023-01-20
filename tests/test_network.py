#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""

import os
from nldi_crawler import source


def test_download(dummy_source):
    """download source data"""
    fname = source.download_geojson(dummy_source)
    assert os.path.exists(fname)
    os.remove(fname)


def test_validation_fail(dummy_source):
    """ give a URI which does not return JSON"""
    dummy_source.feature_uri = "https://www.google.com/"
    result = source.validate_src(dummy_source)
    assert result[0] is False