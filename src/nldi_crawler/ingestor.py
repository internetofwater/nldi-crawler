#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the ingestion of crawler sources
"""
import logging
import ijson

def ingest(src, fname:str):
    logging.info("Ingesting from %s source: %s / %s", src.ingest_type.upper(), src.crawler_source_id, src.source_name)
    try:
        with open(fname, "r", encoding="UTF-8") as read_fh:
            i=1
            for  itm in ijson.items(read_fh, 'features.item'):
                #print(".", end="")
                i+=1
                # if i%80 == 0:
                #     print(" ")
        logging.info("Processed %s features", i-1)
    except ijson.JSONError:
        logging.warning("Parsing error; stopping after %s features read", i-1)
