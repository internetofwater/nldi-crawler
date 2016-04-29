package gov.usgs.owi.nldi.dao.typehandler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.postgis.PGgeometry;
import org.postgis.Point;

import gov.usgs.owi.nldi.BaseSpringTest;

public class PointTypeHandlerTest extends BaseSpringTest {

	@Mock
	protected PreparedStatement ps;

	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void setParameterTest() throws SQLException {
		PointTypeHandler pth = new PointTypeHandler();
		pth.setParameter(ps, 1, new Point(-88.9, 43.3), null);
		verify(ps).setObject(eq(1), any(PGgeometry.class));
	}

	@Test
	public void setNullParameterTest() throws SQLException {
		PointTypeHandler pth = new PointTypeHandler();
		pth.setParameter(ps, 1, null, null);
		verify(ps).setNull(eq(1), anyInt());
	}

}
