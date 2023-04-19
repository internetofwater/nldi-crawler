#!/usr/bin/env python
# coding: utf-8
"""
NOX (The Pythonic make) config to automate test, lint, black, etc.
"""
import nox  # pylint: disable=E0401

# Global options (a.k.a. make defaults)
nox.options.sessions = "lint", "test"

src_locations = "src", "tests", "noxfile.py"


@nox.session(python="3.10")
def reformat(session):
    """Use the Black formatter to pretty-fy souce code."""
    args = session.posargs or src_locations
    session.install("black")
    session.run("black", *args)


@nox.session(python=["3.10"])
def typecheck(session):
    """Static type checking using pytype (a little easier than mypy)"""
    args = session.posargs or ["--disable=import-error", *src_locations]
    session.install("pytype")
    session.run("pytype", *args)


@nox.session(python=["3.10"])
def lint(session):
    """Linter -- prefer pylint over flake8..."""
    args = session.posargs or src_locations
    session.run("poetry", "install", external=True)
    session.install("pylint")
    session.run("pylint", *args)


@nox.session(python=["3.10"])
def test(session):
    """test suite, including coverage report"""
    session.run("poetry", "install", external=True)
    session.run("pytest", "-v", "--cov", "--sparse-ordering", external=True)


@nox.session(python=["3.10"])
def unittest(session):
    """unit-tests only.  Will not execute anything marked as integration."""
    session.run("poetry", "install", external=True)
    session.run(
        "pytest",
        "-v",
        "--cov",
        "-m",
        "not integration",
        "--sparse-ordering",
        external=True,
    )


@nox.session(python=["3.10"])
def docs(session):
    """Rudimentary doc build system using sphinx.    TODO: enhance with mkdocs."""
    session.run("poetry", "install", external=True)
    session.install(
        "sphinx", "sphinx-autodoc-typehints", "sphinx-rtd-theme", "sphinxcontrib-mermaid"
    )
    session.run("sphinx-build", "docs", "docs/_build")
