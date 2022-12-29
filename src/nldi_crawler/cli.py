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
import re
import click

from . import source
from . import ingestor

DEFAULT_DB_INFO = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5432",
    "NLDI_DB_USER": "read_only_user",
    "NLDI_DB_NAME": "nldi",
}


@click.group(invoke_without_command=True)
@click.option("-v", "verbose_", count=True, help="Verbose mode.")
@click.option("--config", "conf_", type=click.Path(exists=True), help="Location of config file.")
@click.pass_context
def main(ctx, verbose_, conf_):
    """
    CLI to launch NLDI crawler.

    """
    if ctx.invoked_subcommand is None:
        ## We can redefine what should happen if no sub-command is given.  Here, I just fall
        ## back to usage.  But we could also have a default behavior, such as listing sources.
        ## TODO: decide what should the default behavior be
        click.echo(f"\nYou must supply a COMMAND: {ctx.command.list_commands(ctx)}\n")
        click.echo(ctx.get_help())
        sys.exit(0)

    if verbose_ == 1:
        logging.basicConfig(level=logging.INFO)
    if verbose_ >= 2:
        logging.basicConfig(level=logging.DEBUG)
    logging.info("VERBOSE is %s", verbose_)

    cfg = DEFAULT_DB_INFO
    cfg.update(cfg_from_env())
    if conf_:
        cfg.update(cfg_from_toml(conf_))
    ctx.ensure_object(dict)
    ctx.obj["DB_URL"] = db_url(cfg)


@main.command()
@click.pass_context
def sources(ctx):
    """
    List all available crawler sources and exit.
    """
    print("\nID : Source Name                                    : Type  : URI ")
    print("==  ", "=" * 46, "  =====  ", "=" * 48)
    for src in source.fetch_source_table(ctx.obj["DB_URL"]):
        print(
            f"{src.crawler_source_id:2} :",
            f"{src.source_name[0:48]:46} :",
            f"{src.ingest_type.upper():5} :",
            f"{src.source_uri[0:48]:48}...",
        )


@main.command()
@click.argument("source_id", nargs=1, type=click.STRING, required=False)
@click.pass_context
def validate(ctx, source_id):
    """
    Connect to data source(s) to verify that they can supply data in JSON format.
    """
    logging.info("Validating data source(s)")
    if source_id:
        source_list = source.fetch_source_table(ctx.obj["DB_URL"], selector=source_id)
        if len(source_list) == 0:
            click.echo(f"No source found with ID {source_id}")
    else:
        source_list = source.fetch_source_table(ctx.obj["DB_URL"])
    for src in source_list:
        logging.info("\t%s", src.source_name)


@main.command()
@click.argument("source_id", nargs=1, type=click.STRING)
@click.pass_context
def download(ctx, source_id):
    """
    Download the data associated with a named data source.
    """
    logging.info(" Downloading source %s ", source_id)
    source_list = source.fetch_source_table(ctx.obj["DB_URL"], selector=source_id)
    if len(source_list) == 0:
        click.echo(f"No source found with ID {source_id}")
        return
    fname = source.download_geojson(source_list[0])
    if fname:
        click.echo(f"Source {source_id} downloaded to {fname}")
    else:
        logging.warning("Download FAILED for source %s", source_id)
        sys.exit(-1)


@main.command()
@click.argument("source_id", nargs=1, type=click.STRING)
@click.pass_context
def ingest(ctx, source_id):
    """
    Download and process data associated with a named data source.
    """
    click.echo("INGEST sub-command")
    click.echo(f"Working on source {source_id}")


#     if source_id:
#         click.echo(f"Looking for source ID {source_id}")
#         uri = db_url(cfg)
#         logging.info("Setting up to crawl source %s", source_id)
#         for source in sources.fetch_source_table(uri, selector=source_id):
#             logging.debug("Found a source...%s : %s", source.crawler_source_id, source.source_name)
#             fname = sources.download_geojson(source)
#             if fname:
#                 click.echo(f"Success !! --> {fname} ")
#             else:
#                 click.echo("ABORTED")
#                 sys.exit(-2)
#             ingestor.ingest(source, fname)
#         sys.exit(0)


def db_url(conf: dict) -> str:
    """
    Formats the full database connection URL using the configuration dict.

    :param conf: config information retrieved from env variables or from toml file.
    :type conf: dict
    :return: connection string in URI format
    :rtype: str
    """
    # NOTE: NLDI_DB_PASS may or may not be set, depending on whether the connection needs a password
    # or not.  The other configurables are assumed to be set based on the defaults defined in the
    # global dict DEFAULT_DB_INFO.  If that assumption is proven invalid, we need to do more error
    # trapping here.
    if "NLDI_DB_PASS" in conf:
        _url = (
            f"postgresql://{conf['NLDI_DB_USER']}:{conf['NLDI_DB_PASS']}"
            + f"@{conf['NLDI_DB_HOST']}:{conf['NLDI_DB_PORT']}/{conf['NLDI_DB_NAME']}"
        )
        logging.info(
            "Using DB connection URI: %s", re.sub(r"//([^:]+):.*@", r"//\g<1>:****@", _url)
        )
    else:
        _url = (
            f"postgresql://{conf['NLDI_DB_USER']}@{conf['NLDI_DB_HOST']}:"
            + f"{conf['NLDI_DB_PORT']}/{conf['NLDI_DB_NAME']}"
        )
        logging.info("Using DB connection URI: %s", _url)
    return _url


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
    for (_k, _v) in DEFAULT_DB_INFO.items():
        env_cfg[_k] = os.environ.get(_k, _v)
    if "NLDI_DB_PASS" in os.environ:
        # password is a special case.  There is no default; it must be explicitly set.
        env_cfg["NLDI_DB_PASS"] = os.environ.get("NLDI_DB_PASS")
    return env_cfg
