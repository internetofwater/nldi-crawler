#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
routines to manage the table of crawler_sources
"""
import dataclasses

from sqlalchemy import create_engine, Table, select
from sqlalchemy.orm import DeclarativeBase, Session


@dataclasses.dataclass
class NldiBase(DeclarativeBase):
    """Base class used to create reflected ORM objects."""

    pass


def fetch_source_table(connect_string: str) -> list:
    """
    Fetches a list of crawler sources from the master NLDI-DB database.  The returned list
    holds one or mor CrawlerSource() objects, which are reflected from the database using
    the sqlalchemy ORM.

    :param connect_string: The db URL used to connect to the database
    :type connect_string: str
    :return: A list of sources
    :rtype: list of CrawlerSource objects
    """
    _tbl_name_ = "crawler_source"
    _schema_ = "nldi_data"
    eng = create_engine(connect_string, client_encoding="UTF-8", echo=False, future=True)
    retval = []

    @dataclasses.dataclass
    class CrawlerSource(NldiBase):
        """
        An ORM reflection of the crawler_source table
        """

        __table__ = Table(
            _tbl_name_,  ## <--- name of the table
            NldiBase.metadata,
            autoload_with=eng,  ## <--- this is where the magic happens
            schema=_schema_,  ## <--- only need this if the table is not in
            ##      the default schema.
        )

    stmt = select(CrawlerSource).order_by(CrawlerSource.crawler_source_id)  # pylint: disable=E1101
    with Session(eng) as session:
        for source in session.scalars(stmt):
            retval.append(source)
    eng = None
    return retval
