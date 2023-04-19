# coding: utf-8
# pylint: disable=fixme
#
#
"""
Implements a 'repository' pattern for handling the crawler_source table.
"""

import logging
from typing import Protocol, Optional
import csv
import httpx
from pydantic.dataclasses import dataclass

from sqlalchemy import URL

from sqlalchemy import create_engine, select, String, Integer, Table, Column, MetaData
from sqlalchemy.orm import Session as SQLASession
from sqlalchemy.orm import registry
from sqlalchemy.exc import OperationalError


@dataclass
class CrawlerSource:
    """
    A dataclass representation of a crawler source. Note that this is the `pydantic`
    dataclass, not the standard library dataclass. This is because we want to be able
    to validate fields as the source is loaded.
    """

    crawler_source_id: int
    source_name: str
    source_suffix: str
    source_uri: str
    feature_id: str
    feature_name: str
    feature_uri: str
    feature_reach: Optional[str]
    feature_measure: Optional[str]
    ingest_type: str
    feature_type: str


class SrcRepo(Protocol):  # pylint: disable=unnecessary-elipsis
    """
    Get and list crawler_sources.
    """

    def get(self, sid: int) -> CrawlerSource:
        """Get a single crawler_source by id."""
        ...

    def get_list(self) -> list[CrawlerSource]:
        """List all crawler_sources."""
        ...


class FakeSrcRepo:
    """
    Implements the SrcRepo protocol using a fake in-memory table.
    This is typically done for testing.

    Example:
    >>> repo = FakeSrcRepo()
    >>> itm = repo.get(12)
    >>> print(itm)

    Note that the get and get_list methods refer to the same table, which is
    populated during __init__.  Subclasses need only override __init__ to
    populate the table; the get and get_list methods will work as expected.
    """

    __FAKE_TABLE__ = [
        dict(
            crawler_source_id=12,
            source_name=r"New Mexico Water Data Initative Sites",
            source_suffix="nmwdi-st",
            source_uri=r"https://locations.newmexicowaterdata.org/collections/Things/items?f=json&limit=100000",
            feature_id="id",
            feature_name="name",
            feature_uri="geoconnex",
            feature_reach="",
            feature_measure="",
            ingest_type="point",
            feature_type="point",
        ),
        dict(
            crawler_source_id=13,
            source_name="geoconnex contribution demo sites",
            source_suffix="geoconnex-demo",
            source_uri=r"https://geoconnex-demo-pages.internetofwater.dev/collections/demo-gpkg/items?f=json&limit=10000",
            feature_id="fid",
            feature_name="GNIS_NAME",
            feature_uri="uri",
            feature_reach="NHDPv2ReachCode",
            feature_measure="NHDPv2Measure",
            ingest_type="reach",
            feature_type="hydrolocation",
        ),
    ]

    def __init__(self):
        self.__SRC_TABLE__ = []
        for _src in self.__FAKE_TABLE__:
            self.__SRC_TABLE__.append(CrawlerSource(**_src))

    def get(self, sid: int) -> CrawlerSource:
        """Get a single crawler_source by id."""
        for _src in self.__SRC_TABLE__:
            if _src.crawler_source_id == sid:
                return _src
        raise ValueError(f"Source {sid} not found.")

    def get_list(self) -> list[CrawlerSource]:
        """List all crawler_sources."""
        return self.__SRC_TABLE__


class CSVRepo(FakeSrcRepo):
    """
    Implements the SrcRepo protocol using a CSV file as the source.

    Note that the CSV file must have a header row, and the names need to match the
    dataclass field names.
    """

    def __init__(self, uri: str, delimiter="\t"):
        self.__SRC_TABLE__ = []
        tsv = httpx.get(uri)
        if tsv.status_code != 200:
            raise ValueError(f"Unable to load {uri}")
        for _s in csv.DictReader(tsv.text.splitlines(), delimiter=delimiter):
            self.__SRC_TABLE__.append(CrawlerSource(**_s))
            logging.debug("Loaded source %s", _s["source_name"])


class JSONRepo(FakeSrcRepo):
    """
    Implements the SrcRepo protocol using a JSON file as the source.

    Note that the JSON file must be an array of objects, and the names need to match the
    dataclass field names.
    """

    def __init__(self, uri: str):
        self.__SRC_TABLE__ = []
        json_data = httpx.get(uri)
        if json_data.status_code != 200:
            raise ValueError(f"Unable to load {uri}")
        for _s in json_data.json():
            self.__SRC_TABLE__.append(CrawlerSource(**_s))
            logging.debug("Loaded source %s", _s["source_name"])


class SQLRepo(FakeSrcRepo):
    """
    Implements the SrcRepo protocol using a SQL database as the source.
    """

    def __init__(self, uri: URL | str):
        self.__SRC_TABLE__ = []
        self._metadata = MetaData()
        crawler_source_table = Table(
            "crawler_source",
            self._metadata,
            Column("crawler_source_id", Integer, primary_key=True),
            Column("source_name", String(64)),
            Column("source_suffix", String(16)),
            Column("source_uri", String),
            Column("feature_id", String),
            Column("feature_name", String),
            Column("feature_uri", String),
            Column("feature_reach", String),
            Column("feature_measure", String),
            Column("ingest_type", String(16)),
            Column("feature_type", String),
            schema="nldi_data",
        )

        # We set up this weird mapper_registry thing because we want to be able to bind
        # our CrawlerSource dataclass to the SQL table.
        mapper_registry = registry()
        mapper_registry.map_imperatively(
            CrawlerSource,
            crawler_source_table,
        )
        # A more conventional way to make this binding would  be to create the CrawlerSource
        # class as a declarative base, but that would introduce a dependency on the ORM for
        # all uses of that class, not just the SQL repo.
        # This way, we can use the CrawlerSource class in other contexts without
        # having to pull in the ORM.

        logging.info("Loading crawler sources from %s", uri)

        try:
            _engine = create_engine(uri, client_encoding="UTF-8", echo=False, future=True)
            _stmt = select(CrawlerSource)
            with SQLASession(_engine) as _session:
                for _source in _session.scalars(_stmt):
                    logging.debug("New Source: %s", _source.source_name)
                    self.__SRC_TABLE__.append(CrawlerSource(**_source.__dict__))
        except OperationalError as ex:
            logging.error("Error connecting to database: %s", ex)
            raise ex
        finally:
            mapper_registry.dispose()  # <-- Don't leave without doing this !!!
        ## Note, that the source table definition and the registry we used
        ## to bind the dataclass to the table are both local to this method, and
        ## should not leak to outside scope.
