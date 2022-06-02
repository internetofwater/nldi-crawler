package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.service.Ingestor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import gov.usgs.owi.nldi.BaseIT;

import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This test class tests for potential SQL injection attacks at the 2 points of user input.
 * 1. crawler source table
 * 2. input files from http source
 */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE,
	classes={DbTestConfig.class, IngestDao.class, FeatureDao.class, CrawlerSourceDao.class})
public class SqlInjectionIT extends BaseIT {

	@Autowired
	private IngestDao ingestDao;
	@Autowired
	private FeatureDao featureDao;
	@Autowired
	private CrawlerSourceDao crawlerSourceDao;

	@Autowired
	DataSource dataSource;

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/maliciousSource.xml")
	public void maliciousSource1Test() throws SQLException {
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();

		// verify that the input string is an attempted SQL injection
		statement.execute("select source_suffix from nldi_data.crawler_source where crawler_source_id = 1");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals("; select * from pg_class;", result.getString(1));
		}

		CrawlerSource crawlerSource = crawlerSourceDao.getById(1);

		// verify that all table names are sanitized
		Assertions.assertEquals("feature_selectfrompg_class", crawlerSource.getTableName());
		Assertions.assertEquals("feature_selectfrompg_class_temp", crawlerSource.getTempTableName());
		Assertions.assertEquals("feature_selectfrompg_class_old", crawlerSource.getOldTableName());

		ingestDao.initTempTable(crawlerSource.getTempTableName());

		// verify that the table was created with a sanitized string
		statement.execute("select count(*) from nldi_data.feature_selectfrompg_class_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals(0, result.getInt(1));
		}

		// cleanup table
		statement.execute("drop table if exists nldi_data.feature_selectfrompg_class_temp");
		statement.close();
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/maliciousSource.xml")
	public void maliciousSource2Test() throws SQLException {
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();

		// verify that the input string is an attempted SQL injection
		statement.execute("select source_suffix from nldi_data.crawler_source where crawler_source_id = 2");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals("wqp; select * from pg_class;", result.getString(1));
		}

		CrawlerSource crawlerSource = crawlerSourceDao.getById(2);

		// verify that all table names are sanitized
		Assertions.assertEquals("feature_wqpselectfrompg_class", crawlerSource.getTableName());
		Assertions.assertEquals("feature_wqpselectfrompg_class_temp", crawlerSource.getTempTableName());
		Assertions.assertEquals("feature_wqpselectfrompg_class_old", crawlerSource.getOldTableName());

		ingestDao.initTempTable(crawlerSource.getTempTableName());

		// verify that the table was created with a sanitized string
		statement.execute("select count(*) from nldi_data.feature_wqpselectfrompg_class_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals(0, result.getInt(1));
		}

		// cleanup table
		statement.execute("drop table if exists nldi_data.feature_wqpselectfrompg_class_temp");
		statement.close();
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/maliciousSource.xml")
	public void maliciousSource3Test() throws SQLException {
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();

		// verify that the input string is an attempted SQL injection
		statement.execute("select source_suffix from nldi_data.crawler_source where crawler_source_id = 3");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals("wqp;' select * from pg_class;", result.getString(1));
		}

		CrawlerSource crawlerSource = crawlerSourceDao.getById(3);

		// verify that all table names are sanitized
		Assertions.assertEquals("feature_wqpselectfrompg_class", crawlerSource.getTableName());
		Assertions.assertEquals("feature_wqpselectfrompg_class_temp", crawlerSource.getTempTableName());
		Assertions.assertEquals("feature_wqpselectfrompg_class_old", crawlerSource.getOldTableName());

		ingestDao.initTempTable(crawlerSource.getTempTableName());

		// verify that the table was created with a sanitized string
		statement.execute("select count(*) from nldi_data.feature_wqpselectfrompg_class_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals(0, result.getInt(1));
		}

		// cleanup table
		statement.execute("drop table if exists nldi_data.feature_wqpselectfrompg_class_temp");
		statement.close();
	}

	@Test
	@DatabaseSetup("classpath:/testData/sqlinjection/safeSource.xml")
	public void maliciousFeatureTest() throws SQLException, IOException {
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();

		// create temp table to prepare for data processing
		statement.execute("create table nldi_data.feature_wqp_temp (like nldi_data.feature)");

		CrawlerSource crawlerSource = crawlerSourceDao.getById(1);

		Resource resourceFile = new ClassPathResource("testData/sqlinjection/maliciousFeature.geojson");
		File file = resourceFile.getFile();

		Ingestor ingestor = new Ingestor(ingestDao, featureDao, null);
		ingestor.processSourceData(crawlerSource, file);

		// verify that the single feature has been added
		statement.execute("select count(*) from nldi_data.feature_wqp_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals(1, result.getInt(1));
		}

		// verify that the values were inserted as strings, rather than being executed
		statement.execute("select identifier, name, uri from nldi_data.feature_wqp_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			// identifier
			Assertions.assertEquals("wqp;' drop table nldi_data.feature_wqp_temp;", result.getString(1));
			// name
			Assertions.assertEquals("; select * from pg_class;", result.getString(2));
			// uri
			Assertions.assertEquals("wqp; select * from pg_class;", result.getString(3));
		}

		// verify that the table still exists after attempted drop table
		statement.execute("select count(*) from nldi_data.feature_wqp_temp");
		try (ResultSet result = statement.getResultSet()) {
			result.next();
			Assertions.assertEquals(1, result.getInt(1));
		}

		// cleanup temp table
		statement.execute("drop table if exists nldi_data.feature_wqp_temp");
		statement.close();
	}
}
