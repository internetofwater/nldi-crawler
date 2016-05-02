package gov.usgs.owi.nldi.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.usgs.owi.nldi.domain.CrawlerSource;

@Component
public class IngestDao extends BaseDao {
	
	private static final String NS = "ingest";
	public static final String FEATURE_TABLE_PREFIX = "feature_";
	public static final String FEATURE_TABLE_TEMP_SUFFIX = "_temp";
	public static final String LINK_POINT = ".linkPoint";
	public static final String LINK_REACH_MEASURE = ".linkReachMeasure";
	public static final String TRUNCATE = ".truncate";
	public static final String INSTALL = ".install";
	public static final String FEATURE_TABLE_OLD_SUFFIX = "_old";

	@Autowired
	public IngestDao(SqlSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	@Transactional
	public void installData(CrawlerSource crawlerSource) {
		getSqlSession().update(NS + INSTALL, crawlerSource);
	}

	@Transactional
	public void linkPoint(CrawlerSource crawlerSource) {
		getSqlSession().update(NS + LINK_POINT, crawlerSource);
	}

	@Transactional
	public void linkReachMeasure(CrawlerSource crawlerSource) {
		getSqlSession().update(NS + LINK_REACH_MEASURE, crawlerSource);
	}

	@Transactional
	public void clearTempTable(CrawlerSource crawlerSource) {
		getSqlSession().delete(NS + TRUNCATE, crawlerSource);
	}

}
