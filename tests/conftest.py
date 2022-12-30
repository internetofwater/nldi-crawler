#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test suite configuration file.
"""
import pytest
#import sqlalchemy


@pytest.fixture(scope="session")
def connect_string():
    """Fixture to ease connections"""
    return r"postgresql://nldi_schema_owner:changeMe@172.18.0.1:5432/nldi"


# @pytest.fixture(scope="session")
# def demo_db(connect_string):
#     """Use connect_string to instantiate a db connection."""
#     eng = sqlalchemy.create_engine(connect_string,
#           client_encoding="UTF-8", echo=False, future=True,)
#     yield eng
#     eng.dispose()
