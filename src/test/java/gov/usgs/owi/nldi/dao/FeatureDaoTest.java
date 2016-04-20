package gov.usgs.owi.nldi.dao;

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
import gov.usgs.owi.nldi.domain.Feature;

@Category(DBIntegrationTest.class)
public class FeatureDaoTest extends BaseSpringTest {
	
	@Resource
	private FeatureDao featureDao;

	@Test
	@DatabaseSetup("classpath:/cleanup/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query="select crawler_source_id, identifier, name, uri, location, st_x(location) long, st_y(location) lat from nldi_data.feature_wqp_temp",
			value="classpath:/testResult/featureWqpTemp.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void addFeatureTest() throws URISyntaxException {
		Point point = new Point(-89.4728889, 43.1922222);
		point.setSrid(Feature.DEFAULT_SRID);
		Feature feature = new Feature();
		feature.setCrawlerSource(CrawlerSourceDaoTest.buildTestSource(1));
		feature.setIdentifier("USGS-05427880");
		feature.setName("SIXMILE CREEK AT STATE HIGHWAY 19 NEAR WAUNAKEE,WI");
		feature.setUri("http://cida-eros-wqpdev.er.usgs.gov:8080/wqp/Station/search?mimeType=geojson&siteid=USGS-05427880");
		feature.setPoint(point);
		featureDao.addFeature(feature);
	}

	@Test
	@DatabaseSetup("classpath:/cleanup/featureWqpTemp.xml")
	@ExpectedDatabase(
			table="nldi_data.feature_wqp_temp",
			query="select crawler_source_id, identifier, name, uri, location, st_x(location) long, st_y(location) lat from nldi_data.feature_wqp_temp",
			value="classpath:/testResult/featureWqpTempNullPoint.xml",
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED)
	public void addFeatureNullPointTest() throws URISyntaxException {
		Feature feature = new Feature();
		feature.setCrawlerSource(CrawlerSourceDaoTest.buildTestSource(1));
		feature.setIdentifier("USGS-05427880");
		feature.setName("SIXMILE CREEK AT STATE HIGHWAY 19 NEAR WAUNAKEE,WI");
		feature.setUri("http://cida-eros-wqpdev.er.usgs.gov:8080/wqp/Station/search?mimeType=geojson&siteid=USGS-05427880");
		featureDao.addFeature(feature);
	}

}
