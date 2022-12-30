#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test CLI.
"""
import os
import click.testing
import pytest


from nldi_crawler import cli


def test_main_succeeds():
    """main() runs with no args and yields a zero exit code."""
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main)
    assert result.exit_code == 0


def test_toml_config():
    """parse cfg file"""
    _test_dir = os.path.dirname(os.path.realpath(__file__))
    cfg = cli.cfg_from_toml(os.path.join(_test_dir, r"cfg-test-1.toml"))
    assert cfg is not None
    assert cfg["NLDI_DB_NAME"] == "test1"
    assert cfg["NLDI_DB_USER"] == "nldi_schema_owner"


def test_toml_broken_config():
    """parse cfg file"""
    _test_dir = os.path.dirname(os.path.realpath(__file__))
    cfg = cli.cfg_from_toml(os.path.join(_test_dir, r"cfg-test-2.toml"))
    ## This config file does not have an 'nldi-db' section, so the config dict should be empty.
    with pytest.raises(KeyError):
        assert cfg["NLDI_DB_NAME"] == "test1"
        assert cfg["NLDI_DB_PASS"] == "changeMe"


def test_env_config():
    """set cfg options from environment"""
    os.environ["NLDI_DB_NAME"] = "SET"
    os.environ["NLDI_DB_PASS"] = "secret"
    cfg = cli.cfg_from_env()
    assert cfg["NLDI_DB_NAME"] == "SET"
    assert cfg["NLDI_DB_PASS"] == "secret"

