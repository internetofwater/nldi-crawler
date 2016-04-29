package gov.usgs.owi.nldi.dao.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.postgis.PGgeometry;
import org.postgis.Point;

public class PointTypeHandler implements TypeHandler<Point> {

	@Override
	public void setParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType) throws SQLException {
		if (null == parameter) {
			ps.setNull(i, JdbcType.NULL.TYPE_CODE);
		} else {
			ps.setObject(i, new PGgeometry(parameter));
		}
	}

	@Override
	public Point getResult(ResultSet rs, String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getResult(ResultSet rs, int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getResult(CallableStatement cs, int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
