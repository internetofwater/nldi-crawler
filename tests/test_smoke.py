#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Smoke test for nldi crawler.... just testing to see if things will plug in; no
functional testing will take place here.
"""

import nldi_crawler


def test_smoke():
    """Does the module import at all?"""
    assert nldi_crawler.__version__ is not None
