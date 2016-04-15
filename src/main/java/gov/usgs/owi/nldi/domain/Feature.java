package gov.usgs.owi.nldi.domain;

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

	public final CrawlerSource getCrawlerSource() {
		return crawlerSource;
	}

	public final void setCrawlerSource(CrawlerSource crawlerSource) {
		this.crawlerSource = crawlerSource;
	}

	public final String getIdentifier() {
		return identifier;
	}

	public final void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getUri() {
		return uri;
	}

	public final void setUri(String uri) {
		this.uri = uri;
	}

	public final Integer getComid() {
		return comid;
	}

	public final void setComid(Integer comid) {
		this.comid = comid;
	}

	public final Point getPoint() {
		return point;
	}

	public final void setPoint(Point point) {
		this.point = point;
	}

}
