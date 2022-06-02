package gov.usgs.owi.nldi.dao.typehandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.postgis.PGgeometry;
import org.postgis.Point;

import gov.usgs.owi.nldi.BaseTest;

public class PointTypeHandlerTest extends BaseTest{

	@Mock
	protected PreparedStatement ps;

	@BeforeEach
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
		verify(ps).setNull(eq(1), eq(JdbcType.NULL.TYPE_CODE));
	}

}
