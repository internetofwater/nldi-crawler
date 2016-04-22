package gov.usgs.owi.nldi.springinit;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan(basePackages={"gov.usgs.owi.nldi.domain", "gov.usgs.owi.nldi.dao", "gov.usgs.owi.nldi.service"})
@EnableJms
public class SpringConfig extends WebMvcConfigurerAdapter {

	@Autowired
	DataSource dataSource;

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public InternalResourceViewResolver setupViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix("");		// Making this empty so we can explicitly call each view we require (i.e. .jsp and .xml)

		return resolver;
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		Resource mybatisConfig = new ClassPathResource("mybatis/mybatisConfig.xml");
		sqlSessionFactory.setConfigLocation(mybatisConfig);
		sqlSessionFactory.setDataSource(dataSource);
		Resource[] mappers = new PathMatchingResourcePatternResolver().getResources("mybatis/mappers/**/*.xml");
		sqlSessionFactory.setMapperLocations(mappers);
		return sqlSessionFactory;
	}

}
