"""
Configuration for SPHINX document generator
"""
project = "NLDI Crawler"
author = "USGS"
copyright = f"2023, {author}"
extensions= [
    "sphinx.ext.autodoc",
    "sphinx.ext.napoleon",
    "sphinx_autodoc_typehints",
    "myst_parser",
	"sphinx_rtd_theme",
    'sphinxcontrib.mermaid'
]
html_theme = "sphinx_rtd_theme"
