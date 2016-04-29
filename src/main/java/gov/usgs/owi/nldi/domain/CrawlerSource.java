package gov.usgs.owi.nldi.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usgs.owi.nldi.dao.CrawlerSourceDao;
import gov.usgs.owi.nldi.dao.IngestDao;

@Component
public class CrawlerSource {

	private static CrawlerSourceDao crawlerSourceDao;

	private int id;

	private String sourceName;

	private String sourceSuffix;

	private String sourceUri;

	private String featureId;

	private String featureName;

	private String featureUri;

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

	public final String getSourceSuffix() {
		return sourceSuffix;
	}

	public final void setSourceSuffix(final String inSourceSuffix) {
		sourceSuffix = inSourceSuffix;
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(final String inSourceUri) {
		sourceUri = inSourceUri;
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

	public final String getTableName() {
		//Sanitize the user-provided value and limit it to 4 characters as a sql injection prevention method.
		return IngestDao.FEATURE_TABLE_PREFIX + sourceSuffix.replaceAll("[^\\w]", "").substring(0, 3).toLowerCase();
	}

	public final String getTempTableName() {
		return getTableName() + IngestDao.FEATURE_TABLE_TEMP_SUFFIX;
	}

	public Object getOldTableName() {
		return getTableName() + IngestDao.FEATURE_TABLE_OLD_SUFFIX;
	}

	public static CrawlerSourceDao getDao() {
		return crawlerSourceDao;
	}

	@Autowired
	public void setCrawlerSourceDao(final CrawlerSourceDao inCrawlerSourceDao) {
		crawlerSourceDao = inCrawlerSourceDao;
	}

}
