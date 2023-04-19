#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test CLI.
"""
import os
import logging
import re

import pytest
import click.testing


from nldi_crawler import cli


@pytest.mark.order(50)
def test_main_succeeds():
    """main() runs with no args and yields a zero exit code."""
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main)
    assert result.exit_code == 0

@pytest.mark.order(50)
def test_verbose():
    """sets verbose level"""
    runner = click.testing.CliRunner()
    _ = runner.invoke(cli.main, "-vv")
    assert logging.root.level == logging.DEBUG
    ## NOTE: logging.root details are only modified wif the
    #  call to basicConfig within the invoked command includes 'force=True'


@pytest.mark.order(50)
def test_toml_config():
    """parse cfg file"""
    _test_dir = os.path.dirname(os.path.realpath(__file__))
    cfg = cli.cfg_from_toml(os.path.join(_test_dir, r"cfg-test-1.toml"))
    assert cfg is not None
    assert cfg["NLDI_DB_NAME"] == "test1"
    assert cfg["NLDI_DB_USER"] == "nldi_schema_owner"


@pytest.mark.order(50)
def test_toml_broken_config():
    """parse cfg file"""
    _test_dir = os.path.dirname(os.path.realpath(__file__))
    cfg = cli.cfg_from_toml(os.path.join(_test_dir, r"cfg-test-2.toml"))
    ## This config file does not have an 'nldi-db' section, so the config dict should be empty.
    with pytest.raises(KeyError):
        assert cfg["NLDI_DB_NAME"] == "test1"
        assert cfg["NLDI_DB_PASS"] == "changeMe"


@pytest.mark.order(50)
def test_env_config():
    """set cfg options from environment"""
    os.environ["NLDI_DB_NAME"] = "SET"
    os.environ["NLDI_DB_PASS"] = "secret"
    cfg = cli.cfg_from_env()
    assert cfg["NLDI_DB_NAME"] == "SET"
    assert cfg["NLDI_DB_PASS"] == "secret"


@pytest.mark.order(50)
def test_cli_download(dal):
    """download via cli"""
    os.environ["NLDI_DB_PASS"] = dal.uri.password
    os.environ["NLDI_DB_USER"] = dal.uri.username
    os.environ["NLDI_DB_HOST"] = dal.uri.host
    os.environ["NLDI_DB_PORT"] = str(dal.uri.port)
    os.environ["NLDI_DB_NAME"] = dal.uri.database
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main, args=["download", "13"], env=os.environ)
    assert result.exit_code == 0
    fname = re.sub("Source \d+ downloaded to ", "", result.output).strip()
    assert os.path.exists(fname)
    os.remove(fname)


@pytest.mark.order(50)
def test_cli_sources(dal):
    """download via cli"""
    os.environ["NLDI_DB_PASS"] = dal.uri.password
    os.environ["NLDI_DB_USER"] = dal.uri.username
    os.environ["NLDI_DB_HOST"] = dal.uri.host
    os.environ["NLDI_DB_PORT"] = str(dal.uri.port)
    os.environ["NLDI_DB_NAME"] = dal.uri.database
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main, args=["sources"], env=os.environ)
    assert result.exit_code == 0
    assert "ID : Source Name                                    : Type  : URI" in result.output


@pytest.mark.order(50)
def test_cli_validate(dal):
    """download via cli"""
    os.environ["NLDI_DB_PASS"] = dal.uri.password
    os.environ["NLDI_DB_USER"] = dal.uri.username
    os.environ["NLDI_DB_HOST"] = dal.uri.host
    os.environ["NLDI_DB_PORT"] = str(dal.uri.port)
    os.environ["NLDI_DB_NAME"] = dal.uri.database
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main, args=["validate", "13"], env=os.environ)
    assert result.exit_code == 0
    assert "PASS" in result.output


@pytest.mark.order(50)
def test_cli_display(dal):
    """download via cli"""
    os.environ["NLDI_DB_PASS"] = dal.uri.password
    os.environ["NLDI_DB_USER"] = dal.uri.username
    os.environ["NLDI_DB_HOST"] = dal.uri.host
    os.environ["NLDI_DB_PORT"] = str(dal.uri.port)
    os.environ["NLDI_DB_NAME"] = dal.uri.database
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main, args=["display", "13"], env=os.environ)
    assert result.exit_code == 0
    assert "ID=13" in result.output
