package gov.usgs.owi.nldi.dao;

import static org.mockito.Mockito.when;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import gov.usgs.owi.nldi.BaseIT;

import gov.usgs.owi.nldi.XMLDataSetLoader;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 
 * This class shows that the plpgsql functions have sql injection protection built in them.
 * They are written to treat the passed in value as the table name, and will wrap it with double quotes as needed to enforce that.
 * The tests do not show that embedded double quotes will be escaped due to the limitations of DBUnit.
 */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
	classes={DbTestConfig.class, IngestDao.class, FeatureDao.class})
@DbUnitConfiguration(dataSetLoader = XMLDataSetLoader.class)
public class SqlInjectionIT extends BaseIT{

	public static final String TEST_QUERY = "select crawler_source_id, identifier, location, comid, reachcode, measure from nldi_data.\"feature; select * from pg_class;\"";
	public static final String TEST_QUERY_TEMP = "select crawler_source_id, identifier, location, comid, reachcode, measure from nldi_data.\"feature; select * from pg_class;_temp\"";

	@Autowired
	private IngestDao ingestDao;
	@Autowired
	private FeatureDao featureDao;

	@Mock
	CrawlerSource crawlerSource;

	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
		when(crawlerSource.getId()).thenReturn(2);
		when(crawlerSource.getTableName()).thenReturn("feature; select * from pg_class;");
		when(crawlerSource.getTempTableName()).thenReturn("feature; select * from pg_class;" + IngestDao.FEATURE_TABLE_TEMP_SUFFIX);
		when(crawlerSource.getOldTableName()).thenReturn("feature; select * from pg_class;" + IngestDao.FEATURE_TABLE_OLD_SUFFIX);
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/ingestorDbIntegration.xml")
	@ExpectedDatabase(
			table="nldi_data.feature; select * from pg_class;",
			query=TEST_QUERY,
			value="classpath:/testResult/sqlinjection/ingestorDbIntegration.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void installDataTest() {
		ingestDao.installData(crawlerSource);
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/tempTablePoint.xml")
	@ExpectedDatabase(
			table="nldi_data.feature; select * from pg_class;_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/sqlinjection/tempTablePoint.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkPointTest() {
		ingestDao.linkPoint(crawlerSource);
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/tempTableReach.xml")
	@ExpectedDatabase(
			table="nldi_data.feature; select * from pg_class;_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/sqlinjection/tempTableReach.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkReachMeasureTest() {
		ingestDao.linkReachMeasure(crawlerSource);
	}

	@Test
	@ExpectedDatabase(
			value="classpath:/testResult/sqlinjection/tempTable.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void clearTempTableSqlInjectionTest() {
		ingestDao.clearTempTable(crawlerSource);
	}

	@Test
	@DatabaseSetup("classpath:/cleanup/sqlinjection/tempTable.xml")
	@ExpectedDatabase(
			table="nldi_data.feature; select * from pg_class;_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/sqlinjection/addFeature.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void addFeatureTest() {
		featureDao.addFeature(FeatureDaoIT.buildTestFeature(crawlerSource));
	}

}
