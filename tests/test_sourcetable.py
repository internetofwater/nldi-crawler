#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""
import os
import pytest
from sqlalchemy.exc import SQLAlchemyError

from nldi_crawler import source


def test_list_sources(dal):
    """get table of sources from db"""
    srcs = source.list_sources(dal)
    assert len(srcs) >= 1

