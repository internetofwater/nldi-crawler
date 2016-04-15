package gov.usgs.owi.nldi.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseSpringTest;
import gov.usgs.owi.nldi.DBIntegrationTest;
import gov.usgs.owi.nldi.domain.CrawlerSource;

@Category(DBIntegrationTest.class)
public class CrawlerSourceDaoTest extends BaseSpringTest {

	public static final int TEST_SOURCE_ID = 1;
	public static final String TEST_SOURCE_NAME = "Water Quality Portal";
	public static final String TEST_SOURCE_SUFFIX = "WQP";
	public static final String TEST_SOURCE_URI = "http://cida-eros-wqpdev.er.usgs.gov:8080/wqp/Station/search?mimeType=geojson";
	public static final String TEST_FEATURE_ID = "MonitoringLocationIdentifier";
	public static final String TEST_FEATURE_NAME = "MonitoringLocationName";
	public static final String TEST_FEATURE_URI = "MonitoringLocationUri";
	public static final String TEST_TABLE_NAME = IngestDao.FEATURE_TABLE_PREFIX + "wqp";
	public static final String TEST_TEMP_TABLE_NAME = TEST_TABLE_NAME + IngestDao.FEATURE_TABLE_TEMP_SUFFIX;
	public static final String TEST_OLD_TABLE_NAME = TEST_TABLE_NAME + IngestDao.FEATURE_TABLE_OLD_SUFFIX;

	@Test
	@DatabaseSetup("classpath:/testData/crawlerSource.xml")
	public void getByIdTest() {
		CrawlerSource crawlerSource = CrawlerSource.getDao().getById(1);
		assertEquals(TEST_SOURCE_ID, crawlerSource.getId());
		assertEquals(TEST_SOURCE_NAME, crawlerSource.getSourceName());
		assertEquals(TEST_SOURCE_SUFFIX, crawlerSource.getSourceSuffix());
		assertEquals(TEST_SOURCE_URI, crawlerSource.getSourceUri());
		assertEquals(TEST_FEATURE_ID, crawlerSource.getFeatureId());
		assertEquals(TEST_FEATURE_NAME, crawlerSource.getFeatureName());
		assertEquals(TEST_FEATURE_URI, crawlerSource.getFeatureUri());
		assertEquals(TEST_TABLE_NAME, crawlerSource.getTableName());
		assertEquals(TEST_TEMP_TABLE_NAME, crawlerSource.getTempTableName());
		assertEquals(TEST_OLD_TABLE_NAME, crawlerSource.getOldTableName());
	}

	public static CrawlerSource buildTestSource(int inId) {
		CrawlerSource crawlerSource = new CrawlerSource();
		crawlerSource.setId(inId);
		crawlerSource.setSourceName(TEST_SOURCE_NAME);
		crawlerSource.setSourceSuffix(TEST_SOURCE_SUFFIX);
		crawlerSource.setFeatureId(TEST_FEATURE_ID);
		crawlerSource.setFeatureName(TEST_FEATURE_NAME);
		crawlerSource.setFeatureUri(TEST_FEATURE_URI);
		return crawlerSource;
	}

}
