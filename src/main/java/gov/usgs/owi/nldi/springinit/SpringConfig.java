package gov.usgs.owi.nldi.springinit;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig  {

	@Autowired
	DataSource dataSource;

}
