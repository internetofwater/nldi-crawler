package gov.usgs.owi.nldi.dao;

import java.math.BigDecimal;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.postgis.Point;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseSpringTest;
import gov.usgs.owi.nldi.DBIntegrationTest;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.Feature;

@Category(DBIntegrationTest.class)
public class FeatureDaoTest extends BaseSpringTest {

	public static final String TEST_IDENTIFIER = "USGS-05427880";
	public static final String TEST_NAME = "SIXMILE CREEK AT STATE HIGHWAY 19 NEAR WAUNAKEE,WI";
	public static final String TEST_URI = "http://www.waterqualitydata.us/provider/NWIS/USGS-WI/USGS-05427880/";
	public static final String TEST_REACHCODE = "05030103000218";
	public static final BigDecimal TEST_MEASURE = BigDecimal.valueOf(123.654);
	public static final Point TEST_POINT = new Point(-89.4728889, 43.1922222);
	public static final CrawlerSource TEST_CRAWLER_SOURCE = CrawlerSourceDaoTest.buildTestPointSource(1);

	static {
		TEST_POINT.setSrid(Feature.DEFAULT_SRID);
	}

	public static final String TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp_temp";

	@Resource
	private FeatureDao featureDao;

	@Test
	@DatabaseSetup("classpath:/cleanup/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY,
			value="classpath:/testResult/featureWqpTemp.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void addFeatureTest() throws URISyntaxException {
		featureDao.addFeature(buildTestFeature(TEST_CRAWLER_SOURCE));
	}

	@Test
	@DatabaseSetup("classpath:/cleanup/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query=TEST_QUERY,
			value="classpath:/testResult/featureWqpTempNullPoint.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void addFeatureNullPointTest() throws URISyntaxException {
		Feature feature = buildTestFeature(TEST_CRAWLER_SOURCE);
		feature.setPoint(null);
		featureDao.addFeature(feature);
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
