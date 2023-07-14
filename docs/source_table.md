# Sources Table

Annotations for the `crawler_source` table, which holds information for finding and processing feature sources:

| Type | Column Name | Description |
|------|-------------| ------------|
| integer | crawler_source_id | The unique identifier to differentiate sources in this table. |
| string  | source_name | A human-readable, friendly discriptor for the data source |
| string  | source_suffix | This string is used to build table names internally. It should be a unique string with no special characters |
| string  | source_uri | The web address from which feature data is retrieved. |
| string  | feature_id | The returned GeoJSON from `source_uri` includes feature properties/attributes. This field identifies the name of the property which uniquely identifies the feature within the feature collection. This is treated as the `KEY` within the feature collection |
| string  | feature_name | The property name within the returned GeoJSON which holds the name of the feature. |
| string  | feature_uri  | the property name within the returned GeoJSON which holds the URL by which a feature can be accessed directly. |
| string  | feature_reach | The property name within the returned GeoJSON which holds the reach identifier |
| string  | feature_measure | The property name within the returned GeoJSON which holds the M-value along the `feature_reach` where this feature can be found |
| string  | ingest_type | The type of feature to be parsed.  This string should be one of [ `reach` , `point` ] |
| string  | feature_type | Unknown.  This string is one of [ `hydrolocation` , `point` , `varies` ]

## Example

```sql
SELECT * from nldi_data.crawler_source where crawler_source_id = 10
```

|Source number `10` contains the following data:

|Column | Value |
|-------|-------|
|crawler_source_id | 10
|source_name       | Vigil Network Data
|source_suffix |  vigil
|source_uri |     https://www.sciencebase.gov/catalog/file/get/60c7b895d34e86b9389b2a6c?name=vigil.geojson
|feature_id |    SBID
|feature_name  | Site Name
|feature_uri |    SBURL
|feature_reach |  REACHCODE
|feature_measure | REACH_measure
|ingest_type |   reach
|feature_type |    hydrolocation

If we fetch the GeoJSON for this source, we see that the feature table looks like this:

| SBID | Site Name | SBURL | REACHCODE | REACH_measure | Location | geometry | ... |
|------|-----------|-------|-----------|---------------|----------|----------| ----|
|5fe395bbd34ea5387deb4950 | Aching Shoulder Slope, New Mexico, USA | https://www.sciencebase.gov/catalog/item/5fe395bbd34ea5387deb4950 | null | null | Mitten Rock, New Mexico USA | Point() | ... |
5fe39807d34ea5387deb4970 | Armells Creek, Montana, USA | https://www.sciencebase.gov/catalog/item/5fe39807d34ea5387deb4970 | 10100001000709 | 90.193048735368549 | Yellowstone River Basin, Southeastern Montana, USA | Point() | ... |
|...|
|...|
