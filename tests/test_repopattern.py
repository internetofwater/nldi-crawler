#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test source table functions.
"""
import pytest
import httpx
import sqlalchemy
from sqlalchemy.exc import SQLAlchemyError


from nldi_crawler import source


@pytest.mark.order(5)
def test_fake_repo():
    """test fake repo"""
    repo = source.FakeSrcRepo()
    assert len(repo) == 2
    _s = repo.get(102)
    assert _s.crawler_source_id == 102


@pytest.mark.order(5)
def test_repo_nosuchkey():
    """test fake repo for missing key"""
    repo = source.FakeSrcRepo()
    assert repo.get(999) is None


@pytest.mark.order(5)
def test_csv_repo():
    """test csv repo"""
    tsvsource = (
        r"https://raw.githubusercontent.com/"
        + "internetofwater/nldi-db/master/"
        + "liquibase/changeLogs/nldi/nldi_data/update_crawler_source/crawler_source.tsv"
    )
    repo = source.CSVRepo(tsvsource)
    assert len(repo) >= 1
    _s = repo.get(12)
    assert _s.crawler_source_id == 12


@pytest.mark.order(5)
def test_csv_repo_bad_url():
    """Traps for bad URL"""
    with pytest.raises(ValueError):
        tsvsource = "https://raw.githubusercontent.com/no/such/source.tsv"
        _ = source.CSVRepo(tsvsource)
    with pytest.raises(httpx.UnsupportedProtocol):
        tsvsource = "file://no/such/source.json"
        _ = source.CSVRepo(tsvsource)


@pytest.mark.order(5)
def test_json_repo():
    """Reads JSON crawler source"""
    jsonsource = (
        r"https://raw.githubusercontent.com/"
        + "gzt5142/nldi-crawler-py/python-port/tests/sources.json"
    )
    repo = source.JSONRepo(jsonsource)
    assert len(repo) >= 1
    _s = repo.get(12)
    assert _s.crawler_source_id == 12


@pytest.mark.order(5)
def test_json_repo_bad_url():
    """Traps for bad URL"""
    with pytest.raises(ValueError):
        jsonsource = "https://raw.githubusercontent.com/no/such/source.json"
        _ = source.JSONRepo(jsonsource)
    with pytest.raises(httpx.UnsupportedProtocol):
        jsonsource = "/no/such/source.json"
        _ = source.JSONRepo(jsonsource)


@pytest.mark.order(5)
@pytest.mark.integration
def test_sql_repo(db_uri):
    """test sql repo"""
    repo = source.SQLRepo(db_uri)
    assert len(repo) >= 1


@pytest.mark.order(5)
@pytest.mark.integration
@pytest.mark.xfail(raises=SQLAlchemyError)
def test_sql_repo_bad_url():
    """Generic db failure.  Bad password in this case."""
    url = sqlalchemy.URL.create(
        "postgresql+psycopg2",
        username="nldi_schema_owner",
        password="badpassword",
        host="172.18.0.1",
        port=5432,
        database="nldi",
    )
    _ = source.SQLRepo(url)
