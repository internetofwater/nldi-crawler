package gov.usgs.owi.nldi.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.postgis.Point;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.usgs.owi.nldi.BaseSpringTest;
import gov.usgs.owi.nldi.dao.CrawlerSourceDaoTest;
import gov.usgs.owi.nldi.dao.FeatureDao;
import gov.usgs.owi.nldi.dao.IngestDao;
import gov.usgs.owi.nldi.domain.CrawlerSource;
import gov.usgs.owi.nldi.domain.Feature;

public class IngestorTest extends BaseSpringTest {

	@Mock
	private IngestDao ingestDao;
	@Mock
	private FeatureDao featureDao;
	@Mock
	private HttpUtils httpUtils;
	private Ingestor ingestor;
	Gson gson = new GsonBuilder().create();
	CrawlerSource crawlerSource;
	
	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
		ingestor = new Ingestor(ingestDao, featureDao, httpUtils);
		crawlerSource = CrawlerSourceDaoTest.buildTestSource(1);
	}

	@Test
	public void processSourceDataTest() throws Exception {
		URL url = this.getClass().getResource("/testResult/json/wqp.geojson");
		ingestor.processSourceData(crawlerSource, new File(url.getFile()));
		verify(featureDao, times(2)).addFeature(any(Feature.class));
	}

	@Test
	public void clearTempTableTest() {
		ingestor.clearTempTable(crawlerSource);
		verify(ingestDao).clearTempTable(crawlerSource);
	}

	@Test
	public void linkCatchmentTest() {
		ingestor.linkCatchment(crawlerSource);
		verify(ingestDao).linkCatchment(crawlerSource);
	}

	@Test
	public void installSourceDataTest() {
		ingestor.installSourceData(crawlerSource);
		verify(ingestDao).installData(crawlerSource);
	}

	@Test
	public void getResponseIteratorTest() {
		JsonObject rawData = new JsonObject();
		JsonArray features = new JsonArray();
		features.add("one");
		features.add("two");
		
		assertNull(ingestor.getResponseIterator(null));
		assertNull(ingestor.getResponseIterator(rawData));
		
		rawData.addProperty(Ingestor.GEOJSON_FEATURES, "none");
		assertNull(ingestor.getResponseIterator(rawData));

		rawData.remove(Ingestor.GEOJSON_FEATURES);
		rawData.add(Ingestor.GEOJSON_FEATURES, features);
		Iterator<JsonElement> i = ingestor.getResponseIterator(rawData);
		assertNotNull(i);
		assertTrue(i.hasNext());
		assertTrue("one".equals(i.next().getAsString()));
		assertTrue("two".equals(i.next().getAsString()));
		assertFalse(i.hasNext());
	}

	@Test
	public void getPropertiesTest() {
		JsonObject feature = new JsonObject();
		JsonObject properties = new JsonObject();
		properties.addProperty("one", 1);
		properties.addProperty("two", 2);
		
		assertNull(ingestor.getProperties(null));
		assertNull(ingestor.getProperties(feature));
		
		feature.addProperty(Ingestor.GEOJSON_PROPERTIES, "abc");
		assertNull(ingestor.getProperties(feature));
		
		feature.remove(Ingestor.GEOJSON_PROPERTIES);
		feature.add(Ingestor.GEOJSON_PROPERTIES, properties);
		assertEquals(properties, ingestor.getProperties(feature));
	}
	
	@Test
	public void getPointTest() throws JsonSyntaxException, IOException {
		JsonObject badFeature = new JsonObject();
		JsonObject geometry = new JsonObject(); 
		JsonArray badCoordinatesArray = new JsonArray();
		badCoordinatesArray.add(-90.3);

		assertNull(ingestor.getPoint(null));
		assertNull(ingestor.getPoint(badFeature));
		
		badFeature.addProperty(Ingestor.GEOJSON_GEOMETRY, "bad");
		assertNull(ingestor.getPoint(badFeature));
		
		badFeature.remove(Ingestor.GEOJSON_GEOMETRY);
		badFeature.add(Ingestor.GEOJSON_GEOMETRY, geometry);
		geometry.addProperty(Ingestor.GEOJSON_TYPE, "polygon");
		assertNull(ingestor.getPoint(badFeature));
		
		geometry.remove(Ingestor.GEOJSON_TYPE);
		geometry.addProperty(Ingestor.GEOJSON_TYPE, Ingestor.GEOJSON_TYPE_POINT);
		assertNull(ingestor.getPoint(badFeature));
		
		geometry.addProperty(Ingestor.GEOJSON_COORDINATES, "bad");
		assertNull(ingestor.getPoint(badFeature));
		
		geometry.remove(Ingestor.GEOJSON_COORDINATES);
		geometry.add(Ingestor.GEOJSON_COORDINATES, badCoordinatesArray);
		assertNull(ingestor.getPoint(badFeature));

		badCoordinatesArray.add((String) null);
		assertNull(ingestor.getPoint(badFeature));

		badCoordinatesArray = new JsonArray();
		badCoordinatesArray.add((String) null);
		badCoordinatesArray.add(12.59);
		geometry.remove(Ingestor.GEOJSON_COORDINATES);
		geometry.add(Ingestor.GEOJSON_COORDINATES, badCoordinatesArray);
		assertNull(ingestor.getPoint(badFeature));

		badCoordinatesArray = new JsonArray();
		badCoordinatesArray.add("abc");
		badCoordinatesArray.add("xyz");
		geometry.remove(Ingestor.GEOJSON_COORDINATES);
		geometry.add(Ingestor.GEOJSON_COORDINATES, badCoordinatesArray);
		assertNull(ingestor.getPoint(badFeature));

		JsonObject jsonFeature = gson.fromJson(getSourceFile("singleFeatureWqp.geojson"), JsonObject.class);
		Point point = ingestor.getPoint(jsonFeature);
		assertTrue(Double.valueOf(-93.6208333).equals(point.x));
		assertTrue(Double.valueOf(36.4272222).equals(point.y));	
	}

	@Test
	public void getStringTest() {
		assertNull(ingestor.getString(null, null));
		assertNull(ingestor.getString("abc", null));
		assertNull(ingestor.getString(null, new JsonObject()));
		assertNull(ingestor.getString("abc", new JsonObject()));
		
		JsonObject good = new JsonObject();
		good.addProperty("abc", "valueStr");
		assertEquals("valueStr", ingestor.getString("abc", good));
		
		JsonObject bad = new JsonObject();
		bad.add("abc", good);
		assertNull(ingestor.getString("abc", bad));
	}

	@Test
	public void buildFeatureTest() throws JsonSyntaxException, IOException, URISyntaxException {
		JsonObject jsonFeature = gson.fromJson(getSourceFile("singleFeatureWqp.geojson"), JsonObject.class);
		
		Feature feature = ingestor.buildFeature(null, null);
		assertNull(feature.getCrawlerSource());
		assertNull(feature.getPoint());
		assertNull(feature.getIdentifier());
		assertNull(feature.getName());
		assertNull(feature.getUri());

		feature = ingestor.buildFeature(crawlerSource, jsonFeature);
		assertEquals(crawlerSource, feature.getCrawlerSource());
		assertTrue(Double.valueOf(-93.6208333).equals(feature.getPoint().x));
		assertTrue(Double.valueOf(36.4272222).equals(feature.getPoint().y));
		assertEquals("USGS-07050500", feature.getIdentifier());
		assertEquals("Kings River near Berryville, AR", feature.getName());
		assertEquals("http://waterqualitydata.us/NWIS/USGS-AR/USGS-07050500", feature.getUri());
	}

}
