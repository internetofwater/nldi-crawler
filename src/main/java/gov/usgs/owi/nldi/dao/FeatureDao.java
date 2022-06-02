package gov.usgs.owi.nldi.dao;

import gov.usgs.owi.nldi.domain.CrawlerSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.usgs.owi.nldi.domain.Feature;

import java.util.HashMap;
import java.util.Map;

@Component
public class FeatureDao extends BaseDao {
	private static final Logger LOG = LoggerFactory.getLogger(FeatureDao.class);

	// mybatis namespace
	private static final String NS = "feature.";

	@Autowired
	public FeatureDao(SqlSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	@Transactional
	public void addFeature(Feature feature, CrawlerSource crawlerSource) {
		LOG.trace("Adding feature with id '" + feature.getIdentifier() + "'");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tempTableName", crawlerSource.getTempTableName());
		parameters.put("crawlerSourceId", crawlerSource.getId());
		parameters.put("identifier", feature.getIdentifier());
		parameters.put("name", feature.getName());
		parameters.put("uri", feature.getUri());
		parameters.put("point", feature.getPoint());
		parameters.put("reachcode", feature.getReachcode());
		parameters.put("measure", feature.getMeasure());
		parameters.put("shape", feature.getShape());

		getSqlSession().insert(NS + "add", parameters);
	}

}
