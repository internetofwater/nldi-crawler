#!/usr/bin/env python
# coding: utf-8
# pylint: disable=fixme
#
#
"""
Configuration handling for the crawler
"""

from __future__ import annotations
from collections import UserDict
import logging
import os
import configparser


DEFAULT_DB_INFO: dict[str, str] = {
    "NLDI_DB_HOST": "localhost",
    "NLDI_DB_PORT": "5432",
    "NLDI_DB_USER": "read_only_user",
    "NLDI_DB_NAME": "nldi",
}


class CrawlerConfig(UserDict):
    """
    Custom dict-like object to get config info.  Can read config from environment or from toml file.
    Can also be used as if a dictionary to set config values explicitly.
    If a key value is not set, it will take the default value from DEFAULT_DB_INFO.

    Example usage:
    >>> cfg = CrawlerConfig.from_env()

    >>> cfg = CrawlerConfig.from_toml("config.toml")

    >>> cfg = CrawlerConfig()
    >>> cfg["NLDI_DB_HOST"] = "localhost"
    >>> cfg["NLDI_DB_PORT"] = "5432"
    """

    def __init__(self, *args, **kwargs) -> None:
        super().__init__(*args, **kwargs)
        for _k, _v in DEFAULT_DB_INFO.items():
            self.setdefault(_k, _v)

    @classmethod
    def from_toml(cls, filepath: str) -> CrawlerConfig:
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
            return cls(retval)
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
        return cls(retval)

    @classmethod
    def from_env(cls) -> CrawlerConfig:
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
            env_cfg["NLDI_DB_PASS"] = os.environ.get("NLDI_DB_PASS", "")
        return cls(env_cfg)
