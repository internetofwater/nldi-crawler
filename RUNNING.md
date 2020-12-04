## Summary
The NLDI Crawler is a tool used to collect new geojson features from a remote data source and place them into an existing NLDI database.

## How to Use
There are a few options to run the crawler and they are described in this repository’s [README](https://github.com/ACWI-SSWD/nldi-crawler). In my case, running the crawler from the built jar file proved to be the easiest due to the database being a locally running docker container. Refer to the [nldi-db repository](https://github.com/ACWI-SSWD/nldi-db) if you would like to run a demo database. 

The inputs necessary to run the crawler are the following: 

* nldiDbHost – address to the database host (e.g. 127.0.0.1) 

* nldiDbPort – port used to connect to the database (e.g. 5446) 

* nldiDbUsername – username used to access the database (e.g. nldi) 

* nldiDbPassword – password associated with the username (e.g. changeMe) 

* CRAWLER_SOURCE_ID – refers to the data source to connect to ([available sources](https://github.com/ACWI-SSWD/nldi-db/blob/master/liquibase/changeLogs/nldi/nldi_data/update_crawler_source/crawler_source.tsv)) 

 

Example command: 
```
nldiDbHost=127.0.0.1 nldiDbPort=5446 nldiDbUsername=nldi nldiDbPassword=changeMe java -jar <path-to-jar>/<jar-name>.jar 6 
```
## Overview of Operations
* Gets uri based on provided crawler source id
* Downloads geojson from uri
* Loads features from geojson file
* Updates nldi_data table with features

New features can be seen in `nldi_data.feature` and will have the corresponding `crawler_source_id`.

## Production vs Dev vs QA
The only configuration required to run the crawler against different database environments is to use the corresponding database address and login information.

Example:
```
nldiDbHost=dev.db.site nldiDbPort=5446 nldiDbUsername=nldi nldiDbPassword=changeMe java -jar <path-to-jar>/<jar-name>.jar 6
nldiDbHost=qa.db.site nldiDbPort=5446 nldiDbUsername=nldi nldiDbPassword=changeMe java -jar <path-to-jar>/<jar-name>.jar 5
nldiDbHost=prod.db.site nldiDbPort=5446 nldiDbUsername=nldi nldiDbPassword=changeMe java -jar <path-to-jar>/<jar-name>.jar 1
```
