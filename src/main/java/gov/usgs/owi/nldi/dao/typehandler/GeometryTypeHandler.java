package gov.usgs.owi.nldi.dao.typehandler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mil.nga.sf.geojson.FeatureConverter;
import mil.nga.sf.geojson.Geometry;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class GeometryTypeHandler implements TypeHandler<Geometry> {
  @Override
  public void setParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType)
      throws SQLException {
    if (null == parameter) {
      ps.setNull(i, JdbcType.NULL.TYPE_CODE);
    } else {
      JsonObject json =
          JsonParser.parseString(FeatureConverter.toStringValue(parameter)).getAsJsonObject();

      // Ensure the JSON object has a defined SRID
      // NOTE: This overwrites any existing crs element.
      // If support for alternative crs values is added in the future, this
      // will need to be adjusted to account for that.
      JsonObject type = new JsonObject();
      type.addProperty("type", "name");

      JsonObject srid = new JsonObject();
      srid.addProperty("name", "EPSG:4269");
      type.add("properties", srid);

      json.add("crs", type);

      ps.setObject(i, json.toString());
    }
  }

  @Override
  public Geometry getResult(ResultSet rs, String columnName) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Geometry getResult(ResultSet rs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Geometry getResult(CallableStatement cs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }
}
