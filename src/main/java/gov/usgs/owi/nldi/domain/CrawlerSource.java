package gov.usgs.owi.nldi.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usgs.owi.nldi.dao.CrawlerSourceDao;
import gov.usgs.owi.nldi.dao.IngestDao;

@Component
public class CrawlerSource {

	public static final String FEATURE_TABLE_PREFIX = "feature_";
	public static final String FEATURE_TABLE_TEMP_SUFFIX = "_temp";
	public static final String FEATURE_TABLE_OLD_SUFFIX = "_old";

	private static CrawlerSourceDao crawlerSourceDao;

	private int id;

	private String sourceName;

	private String sourceSuffix;

	private String sourceUri;

	private IngestType ingestType;

	private String featureId;

	private String featureName;

	private String featureUri;

	private String featureReach;

	private String featureMeasure;

	public int getId() {
		return id;
	}

	public void setId(final int inId) {
		id = inId;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(final String inSourceName) {
		sourceName = inSourceName;
	}

	public String getSourceSuffix() {
		return sourceSuffix;
	}

	public void setSourceSuffix(final String inSourceSuffix) {
		sourceSuffix = inSourceSuffix;
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(final String inSourceUri) {
		sourceUri = inSourceUri;
	}

	public IngestType getIngestType() {
		return ingestType;
	}

	public void setIngestType(final IngestType inIngestType) {
		ingestType = inIngestType;
	}

	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(final String inFeatureId) {
		featureId = inFeatureId;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(final String inFeatureName) {
		featureName = inFeatureName;
	}

	public String getFeatureUri() {
		return featureUri;
	}

	public void setFeatureUri(final String inFeatureUri) {
		featureUri = inFeatureUri;
	}

	public String getFeatureReach() {
		return featureReach;
	}

	public void setFeatureReach(final String inFeatureReach) {
		featureReach = inFeatureReach;
	}

	public String getFeatureMeasure() {
		return featureMeasure;
	}

	public void setFeatureMeasure(final String inFeatureMeasure) {
		featureMeasure = inFeatureMeasure;
	}

	public String getTableName() {
		//Sanitize the user-provided value as a sql injection prevention method.
		String cleanSuffix = sourceSuffix.replaceAll("[^\\w]", "").toLowerCase();
		return FEATURE_TABLE_PREFIX + cleanSuffix;
	}

	public String getTempTableName() {
		return getTableName() + FEATURE_TABLE_TEMP_SUFFIX;
	}

	public Object getOldTableName() {
		return getTableName() + FEATURE_TABLE_OLD_SUFFIX;
	}

	public static CrawlerSourceDao getDao() {
		return crawlerSourceDao;
	}

	@Autowired
	public void setCrawlerSourceDao(final CrawlerSourceDao inCrawlerSourceDao) {
		crawlerSourceDao = inCrawlerSourceDao;
	}

}
