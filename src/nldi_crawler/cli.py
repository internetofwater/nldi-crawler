#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
import click

from . import __version__

@click.command()
@click.version_option(version=__version__)
def main():
    """CLI to launch NLDI crawler."""
    click.echo("Hello World.")
    