#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Command Line Interface for launching the NLDI web crawler.
"""
import sys
import logging
import click

from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.engine import URL

from . import db
from . import source
from . import ingestor
from .config import CrawlerConfig


@click.group(invoke_without_command=True)
@click.option("-v", "verbose_", count=True, help="Verbose mode.")
@click.option("--config", "conf_", type=click.Path(exists=True), help="Location of config file.")
@click.pass_context
def main(ctx: click.Context, verbose_: int, conf_: str) -> None:
    """
    Command-line interface to launch NLDI crawler.
    """
    _lvl = logging.WARNING - (10 * verbose_)
    logging.basicConfig(level=_lvl, force=True)
    logging.info("VERBOSE is %s", verbose_)
    logging.debug("logging.level is %s", logging.root.level)

    if ctx.invoked_subcommand is None:
        click.echo(f"\nYou must supply a COMMAND: {ctx.command.list_commands(ctx)}\n")
        click.echo(ctx.get_help())
        sys.exit(0)

    cfg = CrawlerConfig.from_env()  # pull from env
    if conf_:
        cfg.update(dict(CrawlerConfig.from_toml(conf_)))  # .toml file overrides env
    ctx.ensure_object(dict)

    ctx.obj["DB_URI"] = URL.create(
        "postgresql+psycopg",
        username=cfg["NLDI_DB_USER"],
        password=cfg.get("NLDI_DB_PASS", ""),
        host=cfg["NLDI_DB_HOST"],
        port=int(cfg["NLDI_DB_PORT"]),
        database=cfg["NLDI_DB_NAME"],
    )
    try:
        ctx.obj["SrcRepo"] = source.SQLRepo(ctx.obj["DB_URI"])
        ## NOTE:: we are hard-wired to use the SQL connection to get the source table.
        ##        The repo pattern is made such that we can use other sources if we want, but...
        ##        TODO: configurable source repo (CSV, JSON, etc.)
    except SQLAlchemyError:  # pragma: no coverage
        sys.exit(-2)

    logging.debug(" Using DB connection URI: %s", ctx.obj["DB_URI"])
    ctx.obj["DAL"] = db.DataAccessLayer(ctx.obj["DB_URI"])


@main.command()
@click.pass_context
def sources(ctx: click.Context) -> None:
    """
    List all available crawler sources and exit.
    """
    _srcs = ctx.obj["SrcRepo"]
    print("\nID : Source Name                                    : Type  : URI ")
    print("==  ", "=" * 46, "  =====  ", "=" * 48)
    for _src in _srcs.values():
        print(
            f"{_src.crawler_source_id:2} :",
            f"{_src.source_name[0:48]:46} :",
            f"{_src.ingest_type.upper():5} :",
            f"{_src.source_uri[0:48]:48}...",
        )


@main.command()
@click.argument("source_id", required=False, type=click.INT)
@click.pass_context
def validate(ctx: click.Context, source_id: int) -> None:
    """
    Connect to data source(s) to verify they can return JSON data.
    """
    logging.info("Validating data source(s)")
    src = ctx.obj["SrcRepo"]
    if source_id is None:
        source_list = src.as_list()
    else:
        try:
            source_list = [src.get(int(source_id))]
        except ValueError:
            click.echo(f"Invalid source ID {source_id}")
            sys.exit(-2)

    for src in source_list:
        print(f"{src.crawler_source_id} : Checking {src.source_name}... ", end="")
        result = src.verify()
        if result[0]:
            print(" [PASS]")
        else:
            print(f" [FAIL] : {result[1]}")


@main.command()
@click.argument("source_id", required=True, type=click.INT)
@click.pass_context
def download(ctx: click.Context, source_id: int) -> None:
    """
    Download the data associated with a named data source.
    """
    logging.info(" Downloading source %s ", source_id)
    try:
        src = ctx.obj["SrcRepo"].get(int(source_id))
    except ValueError:
        click.echo(f"Invalid source ID {source_id}")
        sys.exit(-2)

    fname = src.download_geojson()
    if fname != "":
        click.echo(f"Source {source_id} downloaded to {fname}")
    else:
        logging.warning("Download FAILED for source %s", source_id)
        click.echo(f"Download FAILED for source {source_id}")
        sys.exit(-1)


@main.command()
@click.argument("source_id", required=True, type=click.INT)
@click.pass_context
def display(ctx: click.Context, source_id: int) -> None:
    """
    Show details for named data source.
    """
    try:
        src = ctx.obj["SrcRepo"].get(source_id)
    except ValueError:
        click.echo(f"Invalid source ID {source_id}")
        sys.exit(-2)

    print(f"ID={src.crawler_source_id:2} :: {src.source_name}")
    print(f"  Source Suffix  : {src.source_suffix}")
    print(f"  Source URI     : {src.source_uri}")
    print(f"  Feature ID     : {src.feature_id}")
    print(f"  Feature Name   : {src.feature_name}")
    print(f"  Feature URI    : {src.feature_uri}")
    print(f"  Feature Reach  : {src.feature_reach}")
    print(f"  Feature Measure: {src.feature_measure}")
    print(f"  Ingest Type    : {src.ingest_type}")
    print(f"  Feature Type   : {src.feature_type}")


@main.command()
@click.argument("source_id", required=True, type=click.INT)
@click.pass_context
def ingest(ctx: click.Context, source_id: int) -> None:
    """
    Download and process data associated with a named data source.
    """
    logging.info(" Ingesting source %s ", source_id)
    try:
        src = ctx.obj["SrcRepo"].get(source_id)
    except ValueError:
        click.echo(f"Invalid source ID {source_id}")
        sys.exit(-2)

    ingestor.create_tmp_table(ctx.obj["DAL"], src)
    ingestor.sql_ingestor(src, dal=ctx.obj["DAL"])
    ingestor.install_data(ctx.obj["DAL"], src)
