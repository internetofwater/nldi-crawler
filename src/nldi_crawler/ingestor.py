#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the ingestion of crawler sources
"""
import logging
from ijson import JSONError, items

_NAD_83 = 4269
_WGS_84 = 4326
DEFAULT_SRS = _WGS_84


def ingest_from_file(src, fname: str):
    """
    Takes in a source dataset, and processes it to insert into the NLDI-DB feature table

    :param src: The source to be ingested
    :type src: CrawlerSource()
    :param fname: The name of the local copy of the dataset.
    :type fname: str
    """
    logging.info(
        " Ingesting from %s source: %s / %s",
        src.ingest_type.upper(),
        src.crawler_source_id,
        src.source_name,
    )
    try:
        i = 1
        with open(fname, "r", encoding="UTF-8") as read_fh:
            for itm in items(read_fh, "features.item"):
                i += 1
        logging.info(" Processed %s features from %s", i - 1, src.source_name)
    except JSONError:
        logging.warning(" Parsing error; stopping after %s features read", i - 1)
