# coding: utf-8
# pylint: disable=fixme
#
#
"""
Implements a 'repository' pattern for handling the crawler_source table.
"""
import os
import logging
from typing import Protocol, Optional
import tempfile

import re
import csv
import httpx
import ijson

from pydantic.dataclasses import dataclass

from sqlalchemy import URL

from sqlalchemy import create_engine, select, String, Integer, Table, Column, MetaData
from sqlalchemy.orm import Session as SQLASession
from sqlalchemy.orm import registry
from sqlalchemy.exc import OperationalError, SQLAlchemyError


@dataclass
class CrawlerSource:  # pylint: disable=too-many-instance-attributes
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

    def __repr__(self) -> str:
        """
        Returns a string representation of the crawler_source.
        :return: string representation of the crawler_source
        :rtype: str
        """
        return f"{self.__class__.__name__} (id: {self.crawler_source_id}, source_suffix: {self.source_suffix}, feature_type: {self.feature_type})"

    def verify(self) -> tuple:
        """
        Examines a specified source to ensure that it downloads, and the returned data is
        proprly formatted and attributed.

        :return: a tuple of two values: A boolean to indicate if validated, and a string holding
                a description of the reason for failure. If validated is True, the reason string
                is zero-length.
        :rtype: tuple
        """
        try:
            with httpx.stream(
                "GET", self.source_uri, timeout=60.0, follow_redirects=True
            ) as response:
                chunk = response.iter_bytes(2 * 2 * 1024)
                # read 2k bytes, to be sure we get a complete feature.
                itm = next(ijson.items(next(chunk), "features.item"))
                fail = None
                if self.feature_reach is not None and self.feature_reach not in itm["properties"]:
                    fail = (False, f"Column not found for 'feature_reach' : {self.feature_reach}")
                if (
                    self.feature_measure is not None
                    and self.feature_measure not in itm["properties"]
                ):
                    fail = (
                        False,
                        f"Column not found for 'feature_measure' : {self.feature_measure}",
                    )
                if self.feature_name is not None and self.feature_name not in itm["properties"]:
                    fail = (False, f"Column not found for 'feature_name' : {self.feature_name}")
                # A unique feature ID does not have to be in the properties member.  If present,
                # the `id` member is a sibling of `properties`.
                # if self.feature_id is not None and self.feature_id not in itm["properties"]:
                #     fail = (False, f"Column not found for 'feature_id' : {self.feature_id}")
                if self.feature_uri is not None and self.feature_uri not in itm["properties"]:
                    fail = (False, f"Column not found for 'feature_uri' : {self.feature_measure}")
                if fail is not None:
                    return fail
        except httpx.ReadTimeout:
            return (False, "Network Timeout")
        except KeyError:
            return (False, "Key Error")
        except ijson.JSONError:
            return (False, "Invalid JSON")
        return (True, "")

    def download_geojson(self) -> str:
        """
        Downloads data from the specified source, saving it to a temporary file on local disk.

        :return: path name to temporary file
        :rtype: str
        """
        logging.info(" Downloading data from %s ...", self.source_uri)
        fname = "_tmp"
        try:
            with tempfile.NamedTemporaryFile(
                suffix=".geojson",
                prefix=f"CrawlerData_{self.crawler_source_id}_",
                dir=".",
                delete=False,
            ) as tmp_fh:
                fname = tmp_fh.name
                logging.info("Writing to tmp file %s", tmp_fh.name)
                # timeout = 60sec  TODO: make this a tunable
                with httpx.stream(
                    "GET", self.source_uri, timeout=60.0, follow_redirects=True
                ) as response:
                    for chunk in response.iter_bytes(1024):
                        tmp_fh.write(chunk)
        except IOError:
            logging.exception(" I/O Error while downloading from %s to %s", self.source_uri, fname)
            return ""
        except httpx.ReadTimeout:
            logging.critical(" Read TimeOut attempting to download from %s", self.source_uri)
            os.remove(fname)
            return ""
        return fname

    def tablename(self, *args) -> str:
        """
        Getter-like function to return a formatted string representing the table name.
        If an optional positional argument is given, that string is appended to the table name.
        This lets us do things like:
        > self.table_name()
        feature_suffix
        > self.table_name("temp")
        feature_suffix_temp
        > self.table_name("old")
        feature_suffix_old
        :return: name of the table for this crawler_source
        :rtype: string
        """
        # Sanitize the suffix name... only 'word' characters allowed.
        _s = re.sub(r"\W", "_", self.source_suffix)
        if args:
            return "feature_" + _s + "_" + args[0]
        return "feature_" + _s

    def feature_list(self, stream: bool = False):
        """
        Returns a list of features from the crawler_source.
        :return: list of features
        :rtype: list
        """
        if stream:
            raise NotImplementedError("Iterating over the network stream is not yet implemented.")

        _tmpfile = self.download_geojson()
        if not _tmpfile:
            logging.exception("Cannot ingest data from %s", self.source_uri)
            return []

        try:
            with open(_tmpfile, "r", encoding="UTF-8") as fh:
                for feature in ijson.items(fh, "features.item", use_float=True):
                    yield feature
        finally:
            os.remove(_tmpfile)


class SrcRepo:  # pylint: disable=unnecessary-ellipsis
    """
    Get and list crawler_sources.
    """
    def __init__(self):
        self.__SRC_TABLE__ = []

    def get(self, sid: int) -> CrawlerSource:
        """Get a single crawler_source by id."""
        for _src in self.__SRC_TABLE__:
            if _src.crawler_source_id == sid:
                return _src
        raise ValueError(f"Source {sid} not found.")

    def as_list(self) -> list[CrawlerSource]:
        """List all crawler_sources."""
        return self.__SRC_TABLE__

    def __repr__(self) -> str:
        return f"{self.__class__.__name__} (count: {len(self.__SRC_TABLE__)})"


class FakeSrcRepo(SrcRepo):
    """
    Implements the SrcRepo protocol using a fake in-memory table.
    This is typically done for testing.

    Example:
    >>> repo = FakeSrcRepo()
    >>> itm = repo.get(102)
    >>> print(itm)

    Note that the get and get_list methods refer to the same table, which is
    populated during __init__.  Subclasses need only override __init__ to
    populate the table; the get and get_list methods will work as expected.
    """

    __FAKE_TABLE__ = [
        dict(
            crawler_source_id=101,
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
            crawler_source_id=102,
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
        super().__init__()
        for _src in self.__FAKE_TABLE__:
            self.__SRC_TABLE__.append(CrawlerSource(**_src))


class CSVRepo(FakeSrcRepo):
    """
    Implements the SrcRepo protocol using a CSV file as the source.

    Note that the CSV file must have a header row, and the names need to match the
    dataclass field names.
    """

    def __init__(self, uri: str, delimiter="\t"):
        super().__init__()
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
        super().__init__()
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
        super().__init__()
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
        # all uses of the CrawlerSource class, not just the SQL repo.
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
        except OperationalError as ex:   # pragma: no coverage
            logging.error("Error connecting to database: %s", ex)
            raise SQLAlchemyError from ex
        else:
            logging.debug("Loaded %s sources", len(self.__SRC_TABLE__))
        finally:
            mapper_registry.dispose()  # <-- Don't leave without doing this !!!
        ## Note, that the source table definition and the registry we used
        ## to bind the dataclass to the table are both local to this method, and
        ## should not leak to outside scope.
