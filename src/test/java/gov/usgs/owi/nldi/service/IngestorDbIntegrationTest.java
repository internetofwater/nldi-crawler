package gov.usgs.owi.nldi.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.annotation.Resource;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseSpringTest;
import gov.usgs.owi.nldi.DBIntegrationTest;
import gov.usgs.owi.nldi.dao.FeatureDao;
import gov.usgs.owi.nldi.dao.IngestDao;
import gov.usgs.owi.nldi.domain.CrawlerSource;

@Category(DBIntegrationTest.class)
public class IngestorDbIntegrationTest extends BaseSpringTest {

	@Resource
	private IngestDao ingestDao;
	@Resource
	private FeatureDao featureDao;
	@Mock
	private HttpUtils httpUtils;

	private Ingestor ingestor;

	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
		ingestor = new Ingestor(ingestDao, featureDao, httpUtils);
	}

	@Test
	@DatabaseSetup("classpath:/testData/featureWqpTemp.xml")
	@DatabaseSetup("classpath:/testData/crawlerSource.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp",
			query="select crawler_source_id, identifier, name, uri, location, comid, st_x(location) long, st_y(location) lat from nldi_data.feature_wqp_temp",
			value="classpath:/testResult/ingestorDbIntegration.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void dbIntegrationTest() throws ClientProtocolException, IOException {
		URL url = this.getClass().getResource("/testData/wqp.geojson");
		when(httpUtils.callSourceSystem(any(CrawlerSource.class))).thenReturn(new File(url.getFile()));
		ingestor.ingest(1);
	}

}
