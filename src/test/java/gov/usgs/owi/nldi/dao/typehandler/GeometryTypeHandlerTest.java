package gov.usgs.owi.nldi.dao.typehandler;

import gov.usgs.owi.nldi.BaseTest;
import mil.nga.sf.geojson.Geometry;
import mil.nga.sf.geojson.LineString;
import mil.nga.sf.geojson.Point;
import mil.nga.sf.geojson.Position;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.postgis.PGgeometry;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

public class GeometryTypeHandlerTest extends BaseTest {

	@Mock
	protected PreparedStatement ps;

	@BeforeEach
	public void initTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void setGeometryTest() throws SQLException {
		GeometryTypeHandler pth = new GeometryTypeHandler();
		Position position = new Position(-88.9, 43.3);
		Geometry geometry = new Point(position);
		pth.setParameter(ps, 1, geometry, null);
		// sets a geojson string for the parameter
		verify(ps).setObject(eq(1), eq("{\"type\":\"Point\",\"coordinates\":[-88.9,43.3],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4269\"}}}"));
	}

	@Test
	public void setNullTest() throws SQLException {
		GeometryTypeHandler pth = new GeometryTypeHandler();
		pth.setParameter(ps, 1, null, null);
		verify(ps).setNull(eq(1), eq(JdbcType.NULL.TYPE_CODE));
	}

}
