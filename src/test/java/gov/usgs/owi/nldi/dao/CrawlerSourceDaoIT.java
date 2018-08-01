package gov.usgs.owi.nldi.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.IngestType;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
	classes={DbTestConfig.class, CrawlerSourceDao.class})
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class CrawlerSourceDaoIT extends BaseIT {
	
	@Autowired
	CrawlerSourceDao crawlerSourceDao;
	
	public static final int TEST_SOURCE_ID_POINT = 1;
	public static final String TEST_SOURCE_NAME_POINT = "Water Quality Portal";
	public static final String TEST_SOURCE_SUFFIX_POINT = "WQP";
	public static final String TEST_SOURCE_URI_POINT = "http://cida-eros-wqpdev.er.usgs.gov:8080/wqp/Station/search?mimeType=geojson";
	public static final IngestType TEST_INGEST_TYPE_POINT = IngestType.point;
	public static final String TEST_FEATURE_ID_POINT = "MonitoringLocationIdentifier";
	public static final String TEST_FEATURE_NAME_POINT = "MonitoringLocationName";
	public static final String TEST_FEATURE_URI_POINT = "siteUrl";
	public static final String TEST_FEATURE_REACH_POINT = null;
	public static final String TEST_FEATURE_MEASURE_POINT = null;
	public static final String TEST_TABLE_NAME_POINT = IngestDao.FEATURE_TABLE_PREFIX + TEST_SOURCE_SUFFIX_POINT.toLowerCase();
	public static final String TEST_TEMP_TABLE_NAME_POINT = TEST_TABLE_NAME_POINT + IngestDao.FEATURE_TABLE_TEMP_SUFFIX;
	public static final String TEST_OLD_TABLE_NAME_POINT = TEST_TABLE_NAME_POINT + IngestDao.FEATURE_TABLE_OLD_SUFFIX;

	public static final int TEST_SOURCE_ID_REACH = 3;
	public static final String TEST_SOURCE_NAME_REACH = "HNDPlusV2_NWIS_Gages";
	public static final String TEST_SOURCE_SUFFIX_REACH = "np21_nwis";
	public static final String TEST_SOURCE_URI_REACH = "https://www.sciencebase.gov/catalogMaps/mapping/ows/54eb4968e4b02d776a67d8ce?service=WFS&version=1.0.0&request=GetFeature&typeName=sb:nwis_gage_events&outputFormat=json";
	public static final IngestType TEST_INGEST_TYPE_REACH = IngestType.reach;
	public static final String TEST_FEATURE_ID_REACH = "SOURCE_FEA";
	public static final String TEST_FEATURE_NAME_REACH = "SOURCE_FEA";
	public static final String TEST_FEATURE_URI_REACH = "FEATUREDET";
	public static final String TEST_FEATURE_REACH_REACH = "REACHCODE";
	public static final String TEST_FEATURE_MEASURE_REACH = "MEASURE";
	public static final String TEST_TABLE_NAME_REACH = IngestDao.FEATURE_TABLE_PREFIX + TEST_SOURCE_SUFFIX_REACH.toLowerCase();
	public static final String TEST_TEMP_TABLE_NAME_REACH = TEST_TABLE_NAME_REACH + IngestDao.FEATURE_TABLE_TEMP_SUFFIX;
	public static final String TEST_OLD_TABLE_NAME_REACH = TEST_TABLE_NAME_REACH + IngestDao.FEATURE_TABLE_OLD_SUFFIX;

	@Test
	public void getByIdTest() {
		CrawlerSource crawlerSource = crawlerSourceDao.getById(1);
		assertEquals(TEST_SOURCE_ID_POINT, crawlerSource.getId());
		assertEquals(TEST_SOURCE_NAME_POINT, crawlerSource.getSourceName());
		assertEquals(TEST_SOURCE_SUFFIX_POINT, crawlerSource.getSourceSuffix());
		assertEquals(TEST_SOURCE_URI_POINT, crawlerSource.getSourceUri());
		assertEquals(TEST_INGEST_TYPE_POINT, crawlerSource.getIngestType());
		assertEquals(TEST_FEATURE_ID_POINT, crawlerSource.getFeatureId());
		assertEquals(TEST_FEATURE_NAME_POINT, crawlerSource.getFeatureName());
		assertEquals(TEST_FEATURE_URI_POINT, crawlerSource.getFeatureUri());
		assertEquals(TEST_FEATURE_REACH_POINT, crawlerSource.getFeatureReach());
		assertEquals(TEST_FEATURE_MEASURE_POINT, crawlerSource.getFeatureMeasure());
		assertEquals(TEST_TABLE_NAME_POINT, crawlerSource.getTableName());
		assertEquals(TEST_TEMP_TABLE_NAME_POINT, crawlerSource.getTempTableName());
		assertEquals(TEST_OLD_TABLE_NAME_POINT, crawlerSource.getOldTableName());

		crawlerSource = crawlerSourceDao.getById(3);
		assertEquals(TEST_SOURCE_ID_REACH, crawlerSource.getId());
		assertEquals(TEST_SOURCE_NAME_REACH, crawlerSource.getSourceName());
		assertEquals(TEST_SOURCE_SUFFIX_REACH, crawlerSource.getSourceSuffix());
		assertEquals(TEST_SOURCE_URI_REACH, crawlerSource.getSourceUri());
		assertEquals(TEST_INGEST_TYPE_REACH, crawlerSource.getIngestType());
		assertEquals(TEST_FEATURE_ID_REACH, crawlerSource.getFeatureId());
		assertEquals(TEST_FEATURE_NAME_REACH, crawlerSource.getFeatureName());
		assertEquals(TEST_FEATURE_URI_REACH, crawlerSource.getFeatureUri());
		assertEquals(TEST_FEATURE_REACH_REACH, crawlerSource.getFeatureReach());
		assertEquals(TEST_FEATURE_MEASURE_REACH, crawlerSource.getFeatureMeasure());
		assertEquals(TEST_TABLE_NAME_REACH, crawlerSource.getTableName());
		assertEquals(TEST_TEMP_TABLE_NAME_REACH, crawlerSource.getTempTableName());
		assertEquals(TEST_OLD_TABLE_NAME_REACH, crawlerSource.getOldTableName());
	}

	public static CrawlerSource buildTestPointSource(int inId) {
		CrawlerSource crawlerSource = new CrawlerSource();
		crawlerSource.setId(inId);
		crawlerSource.setSourceName(TEST_SOURCE_NAME_POINT);
		crawlerSource.setSourceSuffix(TEST_SOURCE_SUFFIX_POINT);
		crawlerSource.setIngestType(TEST_INGEST_TYPE_POINT);
		crawlerSource.setFeatureId(TEST_FEATURE_ID_POINT);
		crawlerSource.setFeatureName(TEST_FEATURE_NAME_POINT);
		crawlerSource.setFeatureUri(TEST_FEATURE_URI_POINT);
		crawlerSource.setFeatureReach(TEST_FEATURE_REACH_POINT);
		crawlerSource.setFeatureMeasure(TEST_FEATURE_MEASURE_POINT);
		return crawlerSource;
	}

	public static CrawlerSource buildTestReachSource(int inId) {
		CrawlerSource crawlerSource = new CrawlerSource();
		crawlerSource.setId(inId);
		crawlerSource.setSourceName(TEST_SOURCE_NAME_REACH);
		crawlerSource.setSourceSuffix(TEST_SOURCE_SUFFIX_REACH);
		crawlerSource.setIngestType(TEST_INGEST_TYPE_REACH);
		crawlerSource.setFeatureId(TEST_FEATURE_ID_REACH);
		crawlerSource.setFeatureName(TEST_FEATURE_NAME_REACH);
		crawlerSource.setFeatureUri(TEST_FEATURE_URI_REACH);
		crawlerSource.setFeatureReach(TEST_FEATURE_REACH_REACH);
		crawlerSource.setFeatureMeasure(TEST_FEATURE_MEASURE_REACH);
		return crawlerSource;
	}

}
