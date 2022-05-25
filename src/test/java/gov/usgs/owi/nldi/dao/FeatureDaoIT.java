package gov.usgs.owi.nldi.dao;

import java.math.BigDecimal;
import java.net.URISyntaxException;


import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.postgis.Point;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.Feature;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DbTestConfig.class, FeatureDao.class})
public class FeatureDaoIT extends BaseIT {

	public static final String TEST_IDENTIFIER = "USGS-05427880";
	public static final String TEST_NAME = "SIXMILE CREEK AT STATE HIGHWAY 19 NEAR WAUNAKEE,WI";
	public static final String TEST_URI = "http://www.waterqualitydata.us/provider/NWIS/USGS-WI/USGS-05427880/";
	public static final String TEST_REACHCODE = "05030103000218";
	public static final BigDecimal TEST_MEASURE = BigDecimal.valueOf(123.654);
	public static final Point TEST_POINT = new Point(-89.4728889, 43.1922222);
	public static final CrawlerSource TEST_CRAWLER_SOURCE = CrawlerSourceDaoIT.buildTestPointSource(1);

	static {
		TEST_POINT.setSrid(Feature.DEFAULT_SRID);
	}

	public static final String TEST_QUERY = "select crawler_source_id, identifier, name, uri, location, st_x(location) long, st_y(location) lat, reachcode, measure from nldi_data.feature_wqp_temp";

	@Autowired
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
