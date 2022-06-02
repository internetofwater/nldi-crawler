package gov.usgs.owi.nldi.dao;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.Feature;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.NONE,
    classes = {DbTestConfig.class, FeatureDao.class})
public class FeatureDaoIT extends BaseIT {

  public static final String TEST_IDENTIFIER = "USGS-05427880";
  public static final String TEST_NAME = "SIXMILE CREEK AT STATE HIGHWAY 19 NEAR WAUNAKEE,WI";
  public static final String TEST_URI =
      "http://www.waterqualitydata.us/provider/NWIS/USGS-WI/USGS-05427880/";
  public static final String TEST_REACHCODE = "05030103000218";
  public static final BigDecimal TEST_MEASURE = BigDecimal.valueOf(123.654);
  public static final Point TEST_POINT = new Point(-89.4728889, 43.1922222);
  public static final CrawlerSource TEST_CRAWLER_SOURCE =
      CrawlerSourceDaoIT.buildTestPointSource(1);

  static {
    TEST_POINT.setSrid(Feature.DEFAULT_SRID);
  }

  public static final String TEST_QUERY =
      "select crawler_source_id, identifier, name, uri, st_x(location) long, st_y(location) lat,"
          + " reachcode, measure, shape from nldi_data.feature_wqp_temp";
  public static final String TEST_QUERY_NULL =
      "select crawler_source_id, identifier, name, uri, location, reachcode, measure, shape from"
          + " nldi_data.feature_wqp_temp";

  @Autowired private FeatureDao featureDao;
  @Autowired DataSource dataSource;

  @Test
  @DatabaseSetup("classpath:/testData/featureDaoIT/crawlerSource.xml")
  public void addFeatureTest() throws SQLException {
    Statement statement = dataSource.getConnection().createStatement();

    statement.execute(
        "create table nldi_data."
            + TEST_CRAWLER_SOURCE.getTempTableName()
            + " (like nldi_data.feature)");

    // verify the temp table exists and is empty
    statement.execute("select count(*) from nldi_data." + TEST_CRAWLER_SOURCE.getTempTableName());
    try (ResultSet result = statement.getResultSet()) {
      result.next();
      Assertions.assertEquals(0, result.getInt(1));
    }

    Feature testFeature = buildTestFeature(TEST_CRAWLER_SOURCE);
    featureDao.addFeature(testFeature, TEST_CRAWLER_SOURCE);

    // verify the test feature has been added to the temp table
    statement.execute(TEST_QUERY);
    try (ResultSet result = statement.getResultSet()) {
      result.next();
      Assertions.assertEquals(TEST_CRAWLER_SOURCE.getId(), result.getInt("crawler_source_id"));
      Assertions.assertEquals(testFeature.getIdentifier(), result.getString("identifier"));
      Assertions.assertEquals(testFeature.getName(), result.getString("name"));
      Assertions.assertEquals(testFeature.getUri(), result.getString("uri"));
      Assertions.assertEquals(testFeature.getPoint().getX(), result.getDouble("long"));
      Assertions.assertEquals(testFeature.getPoint().getY(), result.getDouble("lat"));
      // special case for comparing BigDecimal to handle floating point precision
      // 0 indicates that they are equal
      Assertions.assertEquals(
          0, testFeature.getReachcode().compareTo(result.getString("reachcode")));
      Assertions.assertEquals(
          0, testFeature.getMeasure().compareTo(result.getBigDecimal("measure")));
      // both should be null in this case
      Assertions.assertEquals(testFeature.getShape(), result.getObject("shape"));
    }

    statement.execute("drop table if exists nldi_data." + TEST_CRAWLER_SOURCE.getTempTableName());
    statement.close();
  }

  @Test
  @DatabaseSetup("classpath:/testData/featureDaoIT/crawlerSource.xml")
  public void addFeatureNullPointTest() throws SQLException {
    Statement statement = dataSource.getConnection().createStatement();

    statement.execute(
        "create table nldi_data."
            + TEST_CRAWLER_SOURCE.getTempTableName()
            + " (like nldi_data.feature)");

    // verify the temp table exists and is empty
    statement.execute("select count(*) from nldi_data." + TEST_CRAWLER_SOURCE.getTempTableName());
    try (ResultSet result = statement.getResultSet()) {
      result.next();
      Assertions.assertEquals(0, result.getInt(1));
    }

    Feature testFeature = buildTestFeature(TEST_CRAWLER_SOURCE);
    testFeature.setPoint(null);
    Assertions.assertNull(testFeature.getPoint());

    featureDao.addFeature(testFeature, TEST_CRAWLER_SOURCE);

    // verify the test feature has been added to the temp table
    statement.execute(TEST_QUERY_NULL);
    try (ResultSet result = statement.getResultSet()) {
      result.next();
      Assertions.assertEquals(TEST_CRAWLER_SOURCE.getId(), result.getInt("crawler_source_id"));
      Assertions.assertEquals(testFeature.getIdentifier(), result.getString("identifier"));
      Assertions.assertEquals(testFeature.getName(), result.getString("name"));
      Assertions.assertEquals(testFeature.getUri(), result.getString("uri"));
      // should be null in this case
      Assertions.assertEquals(testFeature.getPoint(), result.getObject("location"));
      // special case for comparing BigDecimal to handle floating point precision
      // 0 indicates that they are equal
      Assertions.assertEquals(
          0, testFeature.getReachcode().compareTo(result.getString("reachcode")));
      Assertions.assertEquals(
          0, testFeature.getMeasure().compareTo(result.getBigDecimal("measure")));
      // both should be null in this case
      Assertions.assertEquals(testFeature.getShape(), result.getObject("shape"));
    }

    statement.execute("drop table if exists nldi_data." + TEST_CRAWLER_SOURCE.getTempTableName());
    statement.close();
  }

  public static Feature buildTestFeature(CrawlerSource crawlerSource) {
    Feature feature = new Feature();
    feature.setCrawlerSource(crawlerSource);
    feature.setIdentifier(TEST_IDENTIFIER);
    feature.setName(TEST_NAME);
    feature.setUri(TEST_URI);
    feature.setPoint(TEST_POINT);
    feature.setReachcode(TEST_REACHCODE);
    feature.setMeasure(TEST_MEASURE);
    return feature;
  }
}
