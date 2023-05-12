#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Smoke test for nldi crawler.... just testing to see if things will plug in; no
functional testing will take place here.
"""
import pytest
import nldi_crawler


@pytest.mark.order(0)
def test_smoke():
    """Does the module import at all?"""
    assert nldi_crawler.__version__ is not None


@pytest.mark.order(0)
def test_dummy_src(fake_source):
    """verifying that the dummy source fixture works"""
    assert fake_source is not None


@pytest.mark.order(0)
@pytest.mark.integration
def test_data_access_layer(dal):
    """verifying at the dal fixture works"""
    assert dal is not None
