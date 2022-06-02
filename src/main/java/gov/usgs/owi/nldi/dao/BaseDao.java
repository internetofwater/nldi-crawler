package gov.usgs.owi.nldi.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao extends SqlSessionDaoSupport {

	private static final Logger LOG = LoggerFactory.getLogger(BaseDao.class);

	public BaseDao(SqlSessionFactory sqlSessionFactory) {
		LOG.trace(getClass().getName());
		setSqlSessionFactory(sqlSessionFactory);
	}

}
