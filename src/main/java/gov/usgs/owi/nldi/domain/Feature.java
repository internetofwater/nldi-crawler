package gov.usgs.owi.nldi.domain;

import java.math.BigDecimal;

import mil.nga.sf.geojson.Geometry;
import org.postgis.Point;
import org.springframework.stereotype.Component;

@Component
public class Feature {

	public static final int DEFAULT_SRID = 4269;

	private CrawlerSource crawlerSource;

	private String identifier;

	private String name;

	private String uri;

	private Integer comid;

	private Point point;

	private String reachcode;

	private BigDecimal measure;

	private Geometry shape;

	public CrawlerSource getCrawlerSource() {
		return crawlerSource;
	}

	public void setCrawlerSource(final CrawlerSource inCrawlerSource) {
		crawlerSource = inCrawlerSource;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(final String inIdentifier) {
		identifier = inIdentifier;
	}

	public String getName() {
		return name;
	}

	public void setName(final String inName) {
		name = inName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(final String inUri) {
		uri = inUri;
	}

	public Integer getComid() {
		return comid;
	}

	public void setComid(final Integer inComid) {
		comid = inComid;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(final Point inPoint) {
		point = inPoint;
	}

	public String getReachcode() {
		return reachcode;
	}

	public void setReachcode(final String inReachcode) {
		reachcode = inReachcode;
	}

	public BigDecimal getMeasure() {
		return measure;
	}

	public void setMeasure(final BigDecimal inMeasure) {
		measure = inMeasure;
	}

	public Geometry getShape() {
		return shape;
	}

	public void setShape(final Geometry inShape) {
		shape = inShape;
	}

}
