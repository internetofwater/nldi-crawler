# Usage Examples

Launch the tool with no arguments to get a help message.  You get the same thing if you launce with `--help` option.

```text
# nldi-cli

You must supply a COMMAND: ['display', 'download', 'ingest', 'sources', 'validate']

Usage: nldi-cli [OPTIONS] COMMAND [ARGS]...

  CLI to launch NLDI crawler.

Options:
  -v             Verbose mode.
  --config PATH  Location of config file.
  --help         Show this message and exit.

Commands:
  display   Show details for named source.
  download  Download the data associated with a named data source.
  ingest    Download and process data associated with a named data source.
  sources   List all available crawler sources and exit.
  validate  Connect to data source(s) to verify that they can supply data...
```

Note that you can specify `-v` to go into verbose mode.  Using `-vv` will be extra verbose.

The tool connects to a database for all source data.  The details of that database connection can
be configured in two ways:  by setting environment variables, or by supplying a config file.

## CONFIG Option 1

```text
# export NLDI_DB_HOST=172.18.0.1
# export NLDI_DB_PORT=5432
# export NLDI_DB_USER=username
# export NLDI_DB_PASS=secret
# export NLDI_DB_NAME=nldi
#
# nldi-cli sources

ID : Source Name                                    : Type  : URI
==   ==============================================   =====   ================================================
 1 : Water Quality Portal                           : POINT : https://www.waterqualitydata.us/data/Station/sea...
 2 : HUC12 Pour Points                              : POINT : https://www.sciencebase.gov/catalogMaps/mapping/...
 5 : NWIS Surface Water Sites                       : REACH : https://www.sciencebase.gov/catalog/file/get/60c...
 6 : Water Data Exchange 2.0 Sites                  : POINT : https://www.hydroshare.org/resource/5f665b7b82d7...
 7 : geoconnex.us reference gages                   : REACH : https://www.hydroshare.org/resource/3295a17b4cc2...
 8 : Streamgage catalog for CA SB19                 : REACH : https://sb19.linked-data.internetofwater.dev/col...
 9 : USGS Geospatial Fabric V1.1 Points of Interest : REACH : https://www.sciencebase.gov/catalogMaps/mapping/...
10 : Vigil Network Data                             : REACH : https://www.sciencebase.gov/catalog/file/get/60c...
11 : NWIS Groundwater Sites                         : POINT : https://www.sciencebase.gov/catalog/file/get/60c...
12 : New Mexico Water Data Initative Sites          : POINT : https://locations.newmexicowaterdata.org/collect...
13 : geoconnex contribution demo sites              : REACH : https://geoconnex-demo-pages.internetofwater.dev...

```

The `sources` sub-command to the CLI tool will list all of the data sources configured in the database specified
using the `NNDI_DB_` environment variables.

## CONFIG Option #2

You can also specify connection parameters by pointing the tool to a `toml` file.  The contents of that file
should look like this:

```toml
[nldi-db]
hostname: 172.18.0.1
port: 5432
username: username
password: secret
db_name: nldi
```

Use this option with the `--config` switch on the CLI tool:

```text
# nldi-cli -vv --config ./crawler.toml sources
INFO:root:VERBOSE is 2
INFO:root: Consulting environment variables for DB connection info...
INFO:root: Parsing TOML config file ./nldi-crawler.toml for DB connection info...
WARNING:root:Password stored as plain text in crawler.toml. Consider passing as env variable instead.
INFO:root: Using DB connection URI: postgresql://username:****@172.18.0.1:5432/nldi

ID : Source Name                                    : Type  : URI
==   ==============================================   =====   ================================================
 1 : Water Quality Portal                           : POINT : https://www.waterqualitydata.us/data/Station/sea...
 2 : HUC12 Pour Points                              : POINT : https://www.sciencebase.gov/catalogMaps/mapping/...
 5 : NWIS Surface Water Sites                       : REACH : https://www.sciencebase.gov/catalog/file/get/60c...
 6 : Water Data Exchange 2.0 Sites                  : POINT : https://www.hydroshare.org/resource/5f665b7b82d7...
 7 : geoconnex.us reference gages                   : REACH : https://www.hydroshare.org/resource/3295a17b4cc2...
 8 : Streamgage catalog for CA SB19                 : REACH : https://sb19.linked-data.internetofwater.dev/col...
 9 : USGS Geospatial Fabric V1.1 Points of Interest : REACH : https://www.sciencebase.gov/catalogMaps/mapping/...
10 : Vigil Network Data                             : REACH : https://www.sciencebase.gov/catalog/file/get/60c...
11 : NWIS Groundwater Sites                         : POINT : https://www.sciencebase.gov/catalog/file/get/60c...
12 : New Mexico Water Data Initative Sites          : POINT : https://locations.newmexicowaterdata.org/collect...
13 : geoconnex contribution demo sites              : REACH : https://geoconnex-demo-pages.internetofwater.dev...
```

Note the additional output due to the verbose flags being set.

## Display a Source

We can get detailed information about a specific source with the `display` sub-command.  For future examples,
assume the environment has been set to the correct database connection settings.

```text
# nldi-cli display 10
10 :: Vigil Network Data
  Source Suffix:  vigil
  Source URI:     https://www.sciencebase.gov/catalog/file/get/60c7b895d34e86b9389b2a6c?name=vigil.geojson
  Feature ID:     SBID
  Feature Name:   Site Name
  Feature URI:    SBURL
  Feature Reach:  nhdpv2_REACHCODE
  Feature Measure:nhdpv2_REACH_measure
  Ingest Type:    reach
  Feature Type    hydrolocation
```

## Validate a Source

This will attempt to connect to the URI of the source, and parse its output to see that it is valid JSON and
matches the metadata that the source table expects.

```text
# nldi-cli validate 10
Checking Vigil Network Data...  [FAIL] : Column not found for 'feature_reach' : nhdpv2_REACHCODE

# nldi-cli validate 1
Checking Water Quality Portal...  [FAIL] : Network Timeout

# nldi-cli validate 13
Checking geoconnex contribution demo sites...  [PASS]
```

You can also validate all sources at once, by not specifying a specific source:

```text
# nldi-cli -v  validate
INFO:root: VERBOSE is 1
INFO:root: Consulting environment variables for DB connection info...
INFO:root: Parsing TOML config file ./nldi-crawler.toml for DB connection info...
INFO:root: Using DB connection URI: postgresql://username:****@172.18.0.1:5432/nldi
INFO:root:Validating data source(s)
1 : Checking Water Quality Portal...  [FAIL] : Network Timeout
2 : Checking HUC12 Pour Points...  [FAIL] : Invalid JSON
5 : Checking NWIS Surface Water Sites...  [PASS]
6 : Checking Water Data Exchange 2.0 Sites...  [PASS]
7 : Checking geoconnex.us reference gages...  [PASS]
8 : Checking Streamgage catalog for CA SB19...  [FAIL] : Network Timeout
9 : Checking USGS Geospatial Fabric V1.1 Points of Interest...  [PASS]
10 : Checking Vigil Network Data...  [FAIL] : Column not found for 'feature_measure' : nhdpv2_REACH_measure
11 : Checking NWIS Groundwater Sites...  [PASS]
12 : Checking New Mexico Water Data Initative Sites...  [FAIL] : Network Timeout
13 : Checking geoconnex contribution demo sites...  [FAIL] : Column not found for 'feature_id' : id
```

## Download Source Data to Local Disk

This is not especially useful on its own, but can be used for debugging. Specifying a source will download
the GeoJSON from that source to a local file.

```text
# nldi-cli download 10
Source 10 downloaded to /home/trantham/nldi-crawler-py/CrawlerData_10_w2_08yh5.geojson
```

The filename is `CrawlerData_`, plus the the source number, plus a unique random string from the tempfile library, plus `.geojson`.

## Ingesting Source Data

This is the main function of this tool.  It will download source data, then parse it for individual features,
which it will then insert into the master features table in the nldi database.

```text
> nldi-cli -vv --config ./crawler.toml ingest 10
INFO:root:VERBOSE is 2
INFO:root: Consulting environment variables for DB connection info...
INFO:root: Parsing TOML config file ./nldi-crawler.toml for DB connection info...
WARNING:root:Password stored as plain text in nldi-crawler.toml. Consider passing as env variable instead.
INFO:root: Using DB connection URI: postgresql://username:****@172.18.0.1:5432/nldi
INFO:root: Downloading data from https://www.sciencebase.gov/catalog/file/get/60c7b895d34e86b9389b2a6c?name=vigil.geojson ...
INFO:root:Writing to tmp file /home/trantham/nldi-crawler-py/CrawlerData_10_wbkl40e2.geojson
INFO:root: Source 10 dowloaded to /home/trantham/nldi-crawler-py/CrawlerData_10_wbkl40e2.geojson
INFO:root: Ingesting from REACH source: 10 / Vigil Network Data
INFO:root: Processed 70 features from Vigil Network Data
```
