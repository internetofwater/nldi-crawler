package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.domain.CrawlerSource;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IngestDao extends BaseDao {

  // mybatis namespace
  private static final String NS = "ingest.";

  @Autowired
  public IngestDao(SqlSessionFactory sqlSessionFactory) {
    super(sqlSessionFactory);
  }

  @Transactional
  public void installData(CrawlerSource crawlerSource) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("tempTableName", crawlerSource.getTempTableName());
    parameters.put("oldTableName", crawlerSource.getOldTableName());
    parameters.put("tableName", crawlerSource.getTableName());

    getSqlSession().update(NS + "install", parameters);
  }

  @Transactional
  public void linkPoint(@NonNull String tempTableName) {
    getSqlSession().update(NS + "linkPoint", tempTableName);
  }

  @Transactional
  public void linkReachMeasure(@NonNull String tempTableName) {
    getSqlSession().update(NS + "linkReachMeasure", tempTableName);
  }

  @Transactional
  public void initTempTable(@NonNull String tempTableName) {
    getSqlSession().insert(NS + "createTempTable", tempTableName);
    getSqlSession().delete(NS + "truncateTempTable", tempTableName);
  }
}
