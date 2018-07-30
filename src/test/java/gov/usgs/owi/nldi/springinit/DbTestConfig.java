package gov.usgs.owi.nldi.springinit;

import javax.sql.DataSource;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;

import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MybatisConfig.class)
public class DbTestConfig  {

	@Value("${nldiDbUrl}")
	private String datasourceUrl;
	
	@Value("${nldiUsername}")
	private String datasourceUsername;
	
	@Value("${nldiPassword}")
	private String datasourcePassword;
	
	@Value("${dbUnitUsername}")
	private String dbUnitDatasourceUsername;
	
	@Value("${dbUnitPassword}")
	private String dbUnitDatasourcePassword;

	@Bean
	public DataSource dataSource() throws Exception {
		PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setUrl(datasourceUrl);
		ds.setUser(datasourceUsername);
		ds.setPassword(datasourcePassword);
		return ds;
	}

	@Bean
	public DataSource dbUnitDataSource() throws Exception {
		PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setUrl(datasourceUrl);
		ds.setUser(dbUnitDatasourceUsername);
		ds.setPassword(dbUnitDatasourcePassword);
		return ds;
	}

	//Beans to support DBunit for unit testing with PostgreSQL.
	@Bean
	public DatabaseConfigBean dbUnitDatabaseConfig() {
		DatabaseConfigBean dbUnitDbConfig = new DatabaseConfigBean();
		dbUnitDbConfig.setDatatypeFactory(new PostgresqlDataTypeFactory());
		dbUnitDbConfig.setQualifiedTableNames(true);
		dbUnitDbConfig.setEscapePattern("\"?\"");
		return dbUnitDbConfig;
	}

	@Bean
	public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection() throws Exception {
		DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection = new DatabaseDataSourceConnectionFactoryBean();
		dbUnitDatabaseConnection.setDatabaseConfig(dbUnitDatabaseConfig());
		dbUnitDatabaseConnection.setDataSource(dbUnitDataSource());
		return dbUnitDatabaseConnection;
	}

}
