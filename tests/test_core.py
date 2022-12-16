#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
"""
Test suite.
"""

import click.testing

import nldi_crawler

from nldi_crawler import cli


def test_successful_import():
    """Does the module import at all?"""
    assert nldi_crawler.__version__ is not None


def test_main_succeeds():
    """main() runs with no args and yields a zero exit code."""
    runner = click.testing.CliRunner()
    result = runner.invoke(cli.main)
    assert result.exit_code == 0
