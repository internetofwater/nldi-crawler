#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Command Line Interface for launching the NLDI web crawler.
"""
import sys
import os
import logging
import configparser
import click


from . import __version__

DEFAULT_DB_INFO = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5445",
    "NLDI_DB_USER": "nldi_schema_owner",
    "NLDI_DB_NAME": "nldi",
}


@click.command()
@click.option("-v", count=True, help="Verbose mode.")
@click.option("--config", "conf_", type=click.Path(exists=True), help="location of config file.")
@click.option("--list", "list_", is_flag=True, help="Show list of crawler sources and exit.")
@click.version_option(version=__version__)
def main(list_, conf_, verbose):
    """
    CLI to launch NLDI crawler.

    The database connection string is assembled from information in environment variables, or
    from a config file.  If neither are set, will attempt a connection with generic defaults.
    """
    if verbose == 1:
        logging.basicConfig(level=logging.INFO)
    if verbose >= 2:
        logging.basicConfig(level=logging.DEBUG)
    logging.info("verbosity set to %s", verbose)

    cfg = DEFAULT_DB_INFO
    cfg.update(cfg_from_env())
    if conf_:
        cfg.update(cfg_from_toml(conf_))
    if "NLDI_DB_PASS" in cfg:
        db_url = f"postgresql://{cfg['NLDI_DB_USER']}:{cfg['NLDI_DB_PASS']}@{cfg['NLDI_DB_HOST']}:{cfg['NLDI_DB_PORT']}/{cfg['NLDI_DB_NAME']}"
    else:
        db_url = f"postgresql://{cfg['NLDI_DB_USER']}@{cfg['NLDI_DB_HOST']}:{cfg['NLDI_DB_PORT']}/{cfg['NLDI_DB_NAME']}"
    logging.info("Using DB Connect String %s", db_url)

    if list_:
        click.echo("Listing sources.")
        sys.exit(0)


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
    logging.info("Parsing TOML config file %s", filepath)
    retval = {}
    dbconfig = configparser.ConfigParser()
    _ = dbconfig.read(filepath)
    if "nldi-db" not in dbconfig.sections():
        logging.info("No 'nldi-db' section in configuration file %s.", filepath)
        return retval
    nldi_db = dbconfig["nldi-db"]
    if "hostname" in nldi_db:
        retval["NLDI_DB_HOST"] = nldi_db["hostname"].strip("'\"")
    if "port" in nldi_db:
        retval["NLDI_DB_PORT"] = nldi_db["port"].strip("'\"")
    if "username" in nldi_db:
        retval["NLDI_DB_USER"] = nldi_db["username"].strip("'\"")
    if "password" in nldi_db:
        retval["NLDI_DB_PASS"] = nldi_db["password"].strip("'\"")
        logging.warning(
            "Pasword stored as plain text in %s. Consider passing as environment variable instead.",
            os.path.basename(filepath),
        )
    if "db_name" in nldi_db:
        retval["NLDI_DB_NAME"] = nldi_db["db_name"].strip("'\"")
    return retval


def cfg_from_env() -> dict:
    """
    Fetch key configuration values from environment, if set

    :return: dictionary, populated with values.
    :rtype: dict
    """
    logging.info("Fetching config from environment variables...")
    env_cfg = {}
    for (_k, _v) in DEFAULT_DB_INFO.items():
        env_cfg[_k] = os.environ.get(_k, _v)
    if "NLDI_DB_PASS" in os.environ:
        env_cfg["NLDI_DB_PASS"] = os.environ.get("NLDI_DB_PASS")
    return env_cfg
