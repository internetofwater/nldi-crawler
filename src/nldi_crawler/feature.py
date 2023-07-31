#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Defines Feature model
"""
from typing import Optional, Any
from pydantic.dataclasses import dataclass


@dataclass
class CrawledFeature:  # pylint: disable=too-many-instance-attributes
    """
    Defines the Feature model
    """

    comid: Optional[int]
    identifier: Optional[str]
    crawler_source_id: Optional[int]
    name: Optional[str]
    uri: Optional[str]
    reachcode: Optional[str]
    measure: Optional[float]
    location: Any
