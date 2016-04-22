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

	@Resource
	private IngestDao ingestDao;

	@Test
	@DatabaseSetup("classpath:/testData/ingestorDbIntegration.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp",
			query="select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat from nldi_data.feature_wqp",
			value="classpath:/testResult/ingestorDbIntegration.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void installDataTest() {
		ingestDao.installData(CrawlerSourceDaoTest.buildTestSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query="select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat from nldi_data.feature_wqp_temp",
			value="classpath:/testResult/featureWqpTempLinkCatchment.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void linkCatchmentTest() {
		ingestDao.linkCatchment(CrawlerSourceDaoTest.buildTestSource(1));
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@ExpectedDatabase(value="classpath:/cleanup/featureWqpTemp.xml",assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void clearTempTableTest() {
		ingestDao.clearTempTable(CrawlerSourceDaoTest.buildTestSource(1));
	}

}
