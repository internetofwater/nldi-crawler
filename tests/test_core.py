#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test suite.
"""

import nldi_crawler


def test_successful_import():
    """Does the module import at all?"""
    assert nldi_crawler.__version__ is not None
