#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Command Line Interface for launching the NLDI web crawler.
"""
import os
import sys
import logging
import configparser
import click

from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.engine import URL

from . import db
from . import source
from . import ingestor


@click.group(invoke_without_command=True)
@click.option("-v", "verbose_", count=True, help="Verbose mode.")
@click.option("--config", "conf_", type=click.Path(exists=True), help="Location of config file.")
@click.pass_context
def main(ctx, verbose_, conf_):
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

    cfg = cfg_from_env()  # pull from env
    if conf_:
        cfg.update(cfg_from_toml(conf_))  # .toml file overrides env
    ctx.ensure_object(dict)
    ctx.obj["DB_URI"] = URL.create(
        "postgresql+psycopg",
        username=cfg["NLDI_DB_USER"],
        password=cfg.get("NLDI_DB_PASS", ""),
        host=cfg["NLDI_DB_HOST"],
        port=cfg["NLDI_DB_PORT"],
        database=cfg["NLDI_DB_NAME"],
    )
    logging.debug(" Using DB connection URI: %s", ctx.obj["DB_URI"])
    ctx.obj["DAL"] = db.DataAccessLayer(ctx.obj["DB_URI"])


@main.command()
@click.pass_context
def sources(ctx):
    """
    List all available crawler sources and exit.
    """
    print("\nID : Source Name                                    : Type  : URI ")
    print("==  ", "=" * 46, "  =====  ", "=" * 48)
    try:
        for src in source.list_sources(ctx.obj["DAL"]):
            print(
                f"{src.crawler_source_id:2} :",
                f"{src.source_name[0:48]:46} :",
                f"{src.ingest_type.upper():5} :",
                f"{src.source_uri[0:48]:48}...",
            )
    except SQLAlchemyError:
        sys.exit(-2)


@main.command()
@click.argument("source_id", required=False, type=click.INT)
@click.pass_context
def validate(ctx, source_id):
    """
    Connect to data source(s) to verify they can return JSON data.
    """
    logging.info("Validating data source(s)")

    if source_id is None:
        source_list = source.list_sources(ctx.obj["DAL"])
    else:
        source_list = source.list_sources(ctx.obj["DAL"], selector=source_id)
    if len(source_list) == 0:
        click.echo(f"No source found.")

    for src in source_list:
        print(f"{src.crawler_source_id} : Checking {src.source_name}... ", end="")
        result = source.validate_src(src)
        if result[0]:
            print(" [PASS]")
        else:
            print(f" [FAIL] : {result[1]}")


@main.command()
@click.argument("source_id", required=True, type=click.INT)
@click.pass_context
def download(ctx, source_id):
    """
    Download the data associated with a named data source.
    """
    logging.info(" Downloading source %s ", source_id)
    try:
        source_list = source.list_sources(ctx.obj["DAL"], selector=source_id)
    except ConnectionError:
        sys.exit(-2)

    if len(source_list) == 0:
        click.echo(f"No source found with ID {source_id}")
        return
    fname = source.download_geojson(source_list[0])
    if fname:
        click.echo(f"Source {source_id} downloaded to {fname}")
    else:
        logging.warning("Download FAILED for source %s", source_id)
        click.echo(f"Download FAILED for source {source_id}")
        sys.exit(-1)


@main.command()
@click.argument("source_id", required=True, type=click.INT)
@click.pass_context
def display(ctx, source_id):
    """
    Show details for named data source.
    """
    if not source_id:
        return
    try:
        source_list = source.list_sources(ctx.obj["DAL"], selector=source_id)
    except SQLAlchemyError:
        sys.exit(-2)

    if len(source_list) == 0:
        click.echo(f"\nNo source found with ID {source_id}")
        return

    for src in source_list:
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
def ingest(ctx, source_id):
    """
    Download and process data associated with a named data source.
    """
    try:
        source_list = source.list_sources(ctx.obj["DAL"], selector=source_id)
    except SQLAlchemyError:
        sys.exit(-2)

    if len(source_list) == 0:
        click.echo(f"No source found with ID {source_id}")
        return
    fname = source.download_geojson(source_list[0])
    if fname:
        logging.info(" Source %s dowloaded to %s", source_id, fname)
    else:
        logging.warning(" Download FAILED for source %s", source_id)
        sys.exit(-1)
    ingestor.create_tmp_table(ctx.obj["DB_URL"], source_list[0])
    ingestor.ingest_from_file(source_list[0], fname, ctx.obj["DB_URL"])
    ingestor.install_data(ctx.obj["DB_URL"], source_list[0])
    os.remove(fname)


def cfg_from_toml(filepath: str) -> dict:
    """
    Read key configuration values from a TOML-formatted configuration file.
    The config file must contain a 'nldi-db' section, else will return an empty
    dictionary.

    :param filepath: path to toml file
    :type filepath: str
    :return: dictionary holding the config information.
    :rtype: dict
    """
    ## We already know that filepath is valid and points to an existing file, thanks
    ## to click.Path() in the cmdline option spec.
    _section_ = "nldi-db"
    logging.info(" Parsing TOML config file %s for DB connection info...", filepath)
    retval = {}
    dbconfig = configparser.ConfigParser()
    _ = dbconfig.read(filepath)
    if _section_ not in dbconfig.sections():
        logging.info(" No '%s' section in configuration file %s.", _section_, filepath)
        return retval
    retval["NLDI_DB_HOST"] = dbconfig[_section_].get("hostname").strip("'\"")
    retval["NLDI_DB_PORT"] = dbconfig[_section_].get("port").strip("'\"")
    retval["NLDI_DB_USER"] = dbconfig[_section_].get("username").strip("'\"")
    if dbconfig[_section_].get("password") is None:
        logging.debug("No password in TOML file; This is good.")
    else:
        retval["NLDI_DB_PASS"] = dbconfig[_section_].get("password").strip("'\"")
        logging.warning(
            "Password stored as plain text in %s. Consider passing as env variable instead.",
            os.path.basename(filepath),
        )
    retval["NLDI_DB_NAME"] = dbconfig[_section_].get("db_name").strip("'\"")
    return retval


def cfg_from_env() -> dict:
    """
    Fetch key configuration values from environment, if set

    :return: dictionary, populated with values.
    :rtype: dict
    """
    logging.info(" Consulting environment variables for DB connection info...")
    env_cfg = {}
    for (_k, _v) in db.DEFAULT_DB_INFO.items():
        env_cfg[_k] = os.environ.get(_k, _v)
    if "NLDI_DB_PASS" in os.environ:
        # password is a special case.  There is no default; it must be explicitly set.
        env_cfg["NLDI_DB_PASS"] = os.environ.get("NLDI_DB_PASS")
    return env_cfg
