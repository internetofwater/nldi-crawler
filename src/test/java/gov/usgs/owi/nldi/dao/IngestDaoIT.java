package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.domain.CrawlerSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
		classes={DbTestConfig.class, IngestDao.class})
public class IngestDaoIT extends BaseIT {

	public static final String WQP_TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure, shape from nldi_data.feature_wqp";
	public static final String NP21_NWIS_TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, comid, st_y(location) lat, reachcode, measure, shape from nldi_data.feature_np21_nwis";

	@Autowired
	private IngestDao ingestDao;

	@Autowired
	private DataSource dataSource;

	@Test
	public void initTempTableTest() throws SQLException {
		Statement statement = dataSource.getConnection().createStatement();

		String tempTableName = "feature_test_temp";

		// verify that the temp table does not exist
		Exception exception = Assertions.assertThrows(SQLException.class, () -> {
			statement.execute("select * from nldi_data." + tempTableName);
		});
		Assertions.assertTrue(exception.getMessage()
				.contains("relation \"nldi_data." + tempTableName + "\" does not exist"));

		ingestDao.initTempTable(tempTableName);

		// verify the table exists by getting a count of 0
		statement.execute("select count(*) from nldi_data." + tempTableName);
		ResultSet result = statement.getResultSet();
		result.next();
		Assertions.assertEquals(0, result.getInt(1));
		result.close();

		// cleanup by removing temp table
		statement.execute("drop table if exists nldi_data." + tempTableName);
		statement.close();
	}

	@Test
	@DatabaseSetup("classpath:/testData/ingestDaoIT/ingestPointTest.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp",
			query=WQP_TEST_QUERY,
			value="classpath:/testResult/ingestDaoIT/ingestPointTest.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void ingestPointTest() throws SQLException {
		Statement statement = dataSource.getConnection().createStatement();

		// verify that the temp table does not exist
		Exception exception = Assertions.assertThrows(SQLException.class, () -> {
			statement.execute("select * from nldi_data.feature_wqp_temp");
		});
		Assertions.assertTrue(exception.getMessage()
				.contains("relation \"nldi_data.feature_wqp_temp\" does not exist"));

		CrawlerSource crawlerSource = CrawlerSourceDaoIT.buildTestPointSource(1);
		ingestDao.initTempTable(crawlerSource.getTempTableName());

		// verify the table exists by getting a count of 0
		statement.execute("select count(*) from nldi_data.feature_wqp_temp");
		ResultSet result = statement.getResultSet();
		result.next();
		Assertions.assertEquals(0, result.getInt(1));
		result.close();

		moveDataIntoTempTable(statement, crawlerSource.getSourceSuffix());

		ingestDao.linkPoint(crawlerSource.getTempTableName());
		ingestDao.installData(crawlerSource);

		// cleanup by removing temp table
		statement.execute("drop table if exists nldi_data.feature_wqp_temp");
		statement.close();
	}

	@Test
	@DatabaseSetup("classpath:/testData/ingestDaoIT/ingestReachTest.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_np21_nwis",
			query=NP21_NWIS_TEST_QUERY,
			value="classpath:/testResult/ingestDaoIT/ingestReachTest.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void ingestReachTest() throws SQLException {
		Statement statement = dataSource.getConnection().createStatement();

		// verify that the temp table does not exist
		Exception exception = Assertions.assertThrows(SQLException.class, () -> {
			statement.execute("select * from nldi_data.feature_np21_nwis_temp");
		});
		Assertions.assertTrue(exception.getMessage()
				.contains("relation \"nldi_data.feature_np21_nwis_temp\" does not exist"));

		CrawlerSource crawlerSource = CrawlerSourceDaoIT.buildTestReachSource(3);
		ingestDao.initTempTable(crawlerSource.getTempTableName());

		// verify the temp table exists by getting a count of 0
		statement.execute("select count(*) from nldi_data.feature_np21_nwis_temp");
		ResultSet result = statement.getResultSet();
		result.next();
		Assertions.assertEquals(0, result.getInt(1));
		result.close();

		moveDataIntoTempTable(statement, crawlerSource.getSourceSuffix());

		ingestDao.linkReachMeasure(crawlerSource.getTempTableName());
		ingestDao.installData(crawlerSource);

		// cleanup by removing temp table
		statement.execute("drop table if exists nldi_data.feature_np21_nwis_temp");
		statement.close();
	}
}
