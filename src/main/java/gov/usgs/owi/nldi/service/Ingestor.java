package gov.usgs.owi.nldi.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import mil.nga.sf.geojson.FeatureConverter;
import org.postgis.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import gov.usgs.owi.nldi.dao.FeatureDao;
import gov.usgs.owi.nldi.dao.IngestDao;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.Feature;

@Component
public class Ingestor {
	private static final Logger LOG = LoggerFactory.getLogger(Ingestor.class);
	private IngestDao ingestDao;
	private FeatureDao featureDao;
	private HttpUtils httpUtils;

	public static final String GEOJSON_FEATURES = "features";
	public static final String GEOJSON_PROPERTIES = "properties";
	public static final String GEOJSON_GEOMETRY = "geometry";
	public static final String GEOJSON_TYPE = "type";
	public static final String GEOJSON_COORDINATES = "coordinates";

	public static final String GEOJSON_TYPE_POINT = "Point";

	@Autowired
	public Ingestor(IngestDao ingestDao, FeatureDao featureDao, HttpUtils httpUtils) {
		this.ingestDao = ingestDao;
		this.featureDao = featureDao;
		this.httpUtils = httpUtils;
	}

	public void ingest(int sourceID) {
		LOG.trace("Getting crawler source from ID " + sourceID);
		CrawlerSource crawlerSource = CrawlerSource.getDao().getById(sourceID);

		LOG.trace("Initializing temporary table.");
		ingestDao.initTempTable(crawlerSource.getTempTableName());

		File file;

		try {
			LOG.info("Beginning call to " + crawlerSource.getSourceUri() + " for file retrieval.");
			file = httpUtils.callSourceSystem(crawlerSource);
			LOG.info("File retrieval from " + crawlerSource.getSourceUri() + " complete.");
		} catch (IOException exception) {
			LOG.error("File retrieval from " + crawlerSource.getSourceUri() + " failed.");
			LOG.error(exception.getMessage());
			return;
		}

		try {
			processSourceData(crawlerSource, file);
		} catch (Exception exception) {
			LOG.error("Failed processing source data.");
			LOG.error(exception.getMessage());
			return;
		}

		linkCatchment(crawlerSource);

		LOG.info("Beginning install of the new data.");
		ingestDao.installData(crawlerSource);
		LOG.info("Finished installing the new data.");
	}

	public void processSourceData(CrawlerSource crawlerSource, File file) throws JsonIOException, JsonSyntaxException, IOException {
		LOG.info("Beginning processing of source data.");

		// We are expecting GeoJSON
		// with source specific properties registered in the crawler_source table.
		Gson gson = new GsonBuilder().create();
		int count = 0;

		JsonReader reader = new JsonReader(new FileReader(file));
		
		reader.beginObject();
		//First, get to the "features" array.
		Boolean foundFeatures = false;
		 do {
			JsonToken token = reader.peek();
			switch (token) {
				case NAME:
					String propName = reader.nextName();
					if (propName.equals(GEOJSON_FEATURES)) {
						foundFeatures = true;
					}
					break;
				case BEGIN_ARRAY: 
				case BEGIN_OBJECT:
					reader.skipValue();
					break;
				case STRING:
				case NUMBER:
					reader.nextString();
					break;
				case BOOLEAN:
					reader.nextBoolean();
					break;
				case NULL:
					reader.nextNull();
					break;
			}		
		} while (!foundFeatures && reader.hasNext());

		//Then process it.
		 try {
			 reader.beginArray();
		 } catch (IllegalStateException exception) {
			 throw new JsonIOException("Unable to find features array. Input file may be malformed.");
		 }

		while (reader.hasNext()) {
			JsonObject jsonFeature = gson.fromJson(reader, JsonObject.class);
			featureDao.addFeature(buildFeature(crawlerSource, jsonFeature), crawlerSource);
			count = count + 1;
		}
		reader.endArray();

		reader.close();
		LOG.info("Finished processing a total of " + count + " features.");
	}

	public void linkCatchment(CrawlerSource crawlerSource) {
		LOG.info("Beginning process of linking data.");
		switch (crawlerSource.getIngestType()) {
		case point:
			LOG.info("Ingest type recognized as point.");
			ingestDao.linkPoint(crawlerSource.getTempTableName());
			break;
		case reach:
			LOG.info("Ingest type recognized as reach.");
			ingestDao.linkReachMeasure(crawlerSource.getTempTableName());
			break;
		default:
			LOG.info("Ingest type not recognized. Skipping data linking.");
			break;
		}
		LOG.info("Data linking complete.");
	}

	protected Iterator<JsonElement> getResponseIterator(Object obj) {
		Iterator<JsonElement> rtn = null;
		if (obj instanceof JsonObject) {
			JsonObject jsonObject = (JsonObject) obj;
			if (null != jsonObject
					&& jsonObject.has(GEOJSON_FEATURES) && jsonObject.get(GEOJSON_FEATURES).isJsonArray()) {
				rtn = jsonObject.getAsJsonArray(GEOJSON_FEATURES).iterator();
			}
		}
		return rtn;
	}

	protected Feature buildFeature(@NonNull CrawlerSource crawlerSource, JsonObject jsonFeature) {
		Feature feature = new Feature();

		feature.setCrawlerSource(crawlerSource);
		feature.setPoint(getPoint(jsonFeature));
		feature.setShape(getShape(jsonFeature));

		JsonObject properties = getProperties(jsonFeature, crawlerSource.getFeatureId());

		feature.setIdentifier(getString(crawlerSource.getFeatureId(), properties));
		feature.setName(getString(crawlerSource.getFeatureName(), properties));
		feature.setUri(getString(crawlerSource.getFeatureUri(), properties));
		feature.setReachcode(getString(crawlerSource.getFeatureReach(), properties));
		feature.setMeasure(getBigDecimal(crawlerSource.getFeatureMeasure(), properties));

		return feature;
	}

	protected JsonObject getProperties(JsonObject feature, String featureId) {
		JsonObject properties = null;

		if (null != feature
				&& feature.has(GEOJSON_PROPERTIES) && feature.get(GEOJSON_PROPERTIES).isJsonObject()) {
			properties = feature.getAsJsonObject(GEOJSON_PROPERTIES);

			if (null != featureId && !properties.has(featureId) && feature.has(featureId)) {
				properties.addProperty(featureId, feature.get(featureId).getAsString());
			}
		}

		return properties;
	}

	protected Point getPoint(JsonObject feature) {
		JsonObject geometry = null;
		JsonArray coordinates = null;
		Point point = null;

		if (null != feature
				&& feature.has(GEOJSON_GEOMETRY) && feature.get(GEOJSON_GEOMETRY).isJsonObject()) {
			geometry = feature.getAsJsonObject(GEOJSON_GEOMETRY);
		}

		if (null != geometry
				&& geometry.has(GEOJSON_TYPE) && GEOJSON_TYPE_POINT.equalsIgnoreCase(geometry.get(GEOJSON_TYPE).getAsString())
				&& geometry.has(GEOJSON_COORDINATES) && geometry.get(GEOJSON_COORDINATES).isJsonArray()) {
			coordinates = geometry.getAsJsonArray(GEOJSON_COORDINATES);
		}

		if (null != coordinates && 2 == coordinates.size()
				&& !coordinates.get(0).isJsonNull() && !coordinates.get(1).isJsonNull()) {
			try {
				point = new Point(coordinates.get(0).getAsDouble(), coordinates.get(1).getAsDouble());
				point.setSrid(Feature.DEFAULT_SRID);
			} catch (Throwable e) {
				LOG.info("Unable to determine point from coordinates:" + coordinates.get(0).toString() + "-" + coordinates.get(1).toString());
			}
		}
		return point;
	}

	protected mil.nga.sf.geojson.Geometry getShape(@NonNull JsonObject feature) {
		return FeatureConverter.toFeature(feature.toString()).getGeometry();
	}

	protected String getString(String name, JsonObject jsonObject) {
		String value = null;

		if (null != jsonObject
				&& jsonObject.has(name) && jsonObject.get(name).isJsonPrimitive()) {
			value = jsonObject.get(name).getAsString();
		}

		return value;
	}

	protected BigDecimal getBigDecimal(String name, JsonObject jsonObject) {
		BigDecimal value = null;

		if (null != jsonObject
				&& jsonObject.has(name) && jsonObject.get(name).isJsonPrimitive()) {
			try {
				value = jsonObject.get(name).getAsBigDecimal();
			} catch (NumberFormatException e) {
				//Do nothing (return null) if not a valid BigDecimal value.
			}
		}

		return value;
	}

}
