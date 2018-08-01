package gov.usgs.owi.nldi.dao;


import org.junit.Test;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
		classes={DbTestConfig.class, IngestDao.class})
public class IngestDaoIT extends BaseIT {

	public static final String TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp";
	public static final String TEST_QUERY_TEMP = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp_temp";

	@Autowired
	private IngestDao ingestDao;

	@Test
	@DatabaseSetup("classpath:/testData/ingestorDbIntegration.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp",
			query=TEST_QUERY,
			value="classpath:/testResult/ingestorPointDbIntegration.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void installDataTest() {
		ingestDao.installData(CrawlerSourceDaoIT.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/featureWqpTempLinkPoint.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkPointTest() {
		ingestDao.linkPoint(CrawlerSourceDaoIT.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY_TEMP,
			value="classpath:/testResult/featureWqpTempLinkReachMeasure.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkReachMeasureTest() {
		ingestDao.linkReachMeasure(CrawlerSourceDaoIT.buildTestPointSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(value="classpath:/cleanup/featureWqpTemp.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void clearTempTableTest() {
		ingestDao.clearTempTable(CrawlerSourceDaoIT.buildTestPointSource(1));
	}

}
