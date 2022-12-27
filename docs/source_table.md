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
| string  | ingest_type | The type of feature to be parsed.  This string should be one of [`reach`, `point`] |
| string  | feature_type | Unknown.  This string is one of [`hydrolocation`, `point`, `varies`]


