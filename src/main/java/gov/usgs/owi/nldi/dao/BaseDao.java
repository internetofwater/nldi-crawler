package gov.usgs.owi.nldi.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao extends SqlSessionDaoSupport {

	private static final Logger LOG = LoggerFactory.getLogger(BaseDao.class);
	public static final String ADD = ".add";
	public static final String GET_BY_MAP = ".getByMap";
	public static final String GET_BY_ID = ".getById";
	public static final String UPDATE = ".update";
	
	public BaseDao(SqlSessionFactory sqlSessionFactory) {
		LOG.trace(getClass().getName());
		setSqlSessionFactory(sqlSessionFactory);
	}

}
