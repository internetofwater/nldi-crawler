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
from . import sources

DEFAULT_DB_INFO = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5432",
    "NLDI_DB_USER": "nldi_schema_owner",
    "NLDI_DB_NAME": "nldi",
}


@click.command()
@click.option("-v", "verbose_", count=True, help="Verbose mode.")
@click.option("--config", "conf_", type=click.Path(exists=True), help="location of config file.")
@click.option("--list", "list_", is_flag=True, help="Show list of crawler sources and exit.")
@click.version_option(version=__version__)
def main(list_, conf_, verbose_):
    """
    CLI to launch NLDI crawler.

    The database connection string is assembled from information in environment variables, or
    from a config file.  If neither are set, will attempt a connection with generic defaults.
    """
    if verbose_ == 1:
        logging.basicConfig(level=logging.INFO)
    if verbose_ >= 2:
        logging.basicConfig(level=logging.DEBUG)
    logging.info("verbosity set to %s", verbose_)

    cfg = DEFAULT_DB_INFO
    cfg.update(cfg_from_env())
    if conf_:
        cfg.update(cfg_from_toml(conf_))

    if list_:
        for source_item in sources.fetch_source_table(db_url(cfg)):
            print(f"{source_item.crawler_source_id} :: {source_item.source_name} :: {source_item.source_uri[0:64]}...")
        sys.exit(0)


def db_url(c:dict) -> str:
    if "NLDI_DB_PASS" in c:
        db_url = f"postgresql://{c['NLDI_DB_USER']}:{c['NLDI_DB_PASS']}@{c['NLDI_DB_HOST']}:{c['NLDI_DB_PORT']}/{c['NLDI_DB_NAME']}"
    else:
        db_url = f"postgresql://{c['NLDI_DB_USER']}@{c['NLDI_DB_HOST']}:{c['NLDI_DB_PORT']}/{c['NLDI_DB_NAME']}"
    logging.info("Using DB Connect String %s", db_url)
    return db_url

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
    logging.info("Parsing TOML config file %s", filepath)
    retval = {}
    dbconfig = configparser.ConfigParser()
    _ = dbconfig.read(filepath)
    if _section_ not in dbconfig.sections():
        logging.info("No '%s' section in configuration file %s.", _section_, filepath)
        return retval
    retval["NLDI_DB_HOST"] = dbconfig[_section_].get("hostname").strip("'\"")
    retval["NLDI_DB_PORT"] = dbconfig[_section_].get("port").strip("'\"")
    retval["NLDI_DB_USER"] = dbconfig[_section_].get("username").strip("'\"")
    if dbconfig[_section_].get("password") is None:
        logging.debug("No password in TOML file; good.")
    else:
        retval["NLDI_DB_PASS"] = dbconfig[_section_].get("password").strip("'\"")
        logging.warning(
            "Pasword stored as plain text in %s. Consider passing as environment variable instead.",
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
    logging.info("Fetching config from environment variables...")
    env_cfg = {}
    for (_k, _v) in DEFAULT_DB_INFO.items():
        env_cfg[_k] = os.environ.get(_k, _v)
    if "NLDI_DB_PASS" in os.environ:
        # password is a special case.  There is no default; it must be explicitly set.
        env_cfg["NLDI_DB_PASS"] = os.environ.get("NLDI_DB_PASS")
    return env_cfg
