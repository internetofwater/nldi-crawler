package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.domain.CrawlerSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CrawlerSourceDao extends BaseDao {

  private static final String NS = "crawlerSource.";

  @Autowired
  public CrawlerSourceDao(SqlSessionFactory sqlSessionFactory) {
    super(sqlSessionFactory);
  }

  @Transactional(readOnly = true)
  public CrawlerSource getById(int id) {
    return getSqlSession().selectOne(NS + "getById", id);
  }
}
