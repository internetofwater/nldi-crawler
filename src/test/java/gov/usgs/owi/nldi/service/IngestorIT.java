package gov.usgs.owi.nldi.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.dao.CrawlerSourceDao;
import gov.usgs.owi.nldi.dao.FeatureDao;
import gov.usgs.owi.nldi.dao.IngestDao;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {
      DbTestConfig.class,
      IngestDao.class,
      FeatureDao.class,
      CrawlerSourceDao.class,
      CrawlerSource.class
    })
public class IngestorIT extends BaseIT {

  @Autowired private IngestDao ingestDao;
  @Autowired private FeatureDao featureDao;
  @Mock private HttpUtils httpUtils;

  private Ingestor ingestor;

  @BeforeEach
  public void initTest() {
    MockitoAnnotations.initMocks(this);
    ingestor = new Ingestor(ingestDao, featureDao, httpUtils);
  }

  @Test
  @DatabaseSetup("classpath:/testData/ingestorIT/pointDbIntegrationTest.xml")
  @ExpectedDatabase(
      table = "nldi_data.feature_wqp",
      query =
          "select crawler_source_id, identifier, name, uri, location, comid, st_x(location) long,"
              + " st_y(location) lat from nldi_data.feature_wqp",
      value = "classpath:/testResult/ingestorIT/pointDbIntegrationTest.xml",
      assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
  public void pointDbIntegrationTest() throws IOException {
    ClassPathResource resource = new ClassPathResource("/testData/ingestorIT/wqp.geojson");
    when(httpUtils.callSourceSystem(any(CrawlerSource.class))).thenReturn(resource.getFile());

    ingestor.ingest(1);
  }

  @Test
  @DatabaseSetup("classpath:/testData/ingestorIT/reachDbIntegrationTest.xml")
  @ExpectedDatabase(
      table = "nldi_data.feature_np21_nwis",
      query =
          "select crawler_source_id, identifier, name, uri, location, comid, st_x(location) long,"
              + " st_y(location) lat, reachcode, measure from nldi_data.feature_np21_nwis",
      value = "classpath:/testResult/ingestorIT/reachDbIntegrationTest.xml",
      assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
  public void reachDbIntegrationTest() throws IOException {
    ClassPathResource resource = new ClassPathResource("/testData/ingestorIT/np21Nwis.geojson");
    when(httpUtils.callSourceSystem(any(CrawlerSource.class))).thenReturn(resource.getFile());

    ingestor.ingest(3);
  }

  @Test
  @DatabaseSetup("classpath:/testData/ingestorIT/shapeDbIntegrationTest.xml")
  @ExpectedDatabase(
      table = "nldi_data.feature_shape",
      query =
          "select crawler_source_id, identifier, name, uri, comid, reachcode, measure,"
              + " st_astext(shape) shape from nldi_data.feature_shape",
      value = "classpath:/testResult/ingestorIT/shapeDbIntegrationTest.xml",
      assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
  public void shapeDbIntegrationTest() throws IOException {
    ClassPathResource resource = new ClassPathResource("/testData/ingestorIT/shape.geojson");
    when(httpUtils.callSourceSystem(any(CrawlerSource.class))).thenReturn(resource.getFile());

    ingestor.ingest(13);
  }
}
