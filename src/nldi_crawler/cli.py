#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Command Line Interface for launching the NLDI web crawler.
"""

import click

from . import __version__


@click.command()
@click.option("--list", "list_", is_flag=True, help="Show list of crawler sources and exit.")
@click.option("--config","conf_", type=click.Path(exists=True), help="location of config file.")
@click.version_option(version=__version__)
def main(list_, conf_):
    """CLI to launch NLDI crawler."""
    click.echo("Hello World.")
    if (list_):
        click.echo("Listing sources.")
    if (conf_):
        click.echo(f"Using config file: {conf_}")