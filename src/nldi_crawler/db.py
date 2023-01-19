#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
classes and functions surrounding database access
"""
import logging

from sqlalchemy.orm import DeclarativeBase

class NLDI_Base(DeclarativeBase):  # pylint: disable=invalid-name
    """Base class used to create reflected ORM objects."""

