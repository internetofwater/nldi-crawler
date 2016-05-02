package gov.usgs.owi.nldi.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseSpringTest;
import gov.usgs.owi.nldi.DBIntegrationTest;

@Category(DBIntegrationTest.class)
public class IngestDaoTest extends BaseSpringTest {

	public static final String TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp";
	public static final String TEST_QUERY_TEMP = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp_temp";

	@Resource
	private IngestDao ingestDao;

	@Test
	@DatabaseSetup("classpath:/testData/ingestorDbIntegration.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp",
			query=TEST_QUERY,
			value="classpath:/testResult/ingestorPointDbIntegration.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void installDataTest() {
		ingestDao.installData(CrawlerSourceDaoTest.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/featureWqpTempLinkPoint.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkPointTest() {
		ingestDao.linkPoint(CrawlerSourceDaoTest.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/featureWqpTempLinkReachMeasure.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkReachMeasureTest() {
		ingestDao.linkReachMeasure(CrawlerSourceDaoTest.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(value="classpath:/cleanup/featureWqpTemp.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void clearTempTableTest() {
		ingestDao.clearTempTable(CrawlerSourceDaoTest.buildTestPointSource(1));
	}

}
