package gov.usgs.owi.nldi.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.usgs.owi.nldi.domain.Feature;

@Component
public class FeatureDao extends BaseDao {
	private static final Logger LOG = LoggerFactory.getLogger(FeatureDao.class);

	private static final String NS = "feature";

	@Autowired
	public FeatureDao(SqlSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	@Transactional
	public void addFeature(Feature feature) {
		LOG.trace("Adding:" + feature.getIdentifier());
		getSqlSession().insert(NS + ADD, feature);
	}

}
