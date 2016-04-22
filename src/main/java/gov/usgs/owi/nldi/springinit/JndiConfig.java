package gov.usgs.owi.nldi.springinit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JndiConfig {

	private final Context ctx;
	
	public JndiConfig() throws NamingException {
		ctx = new InitialContext();
	}

	@Bean
	public DataSource dataSource() throws Exception {
		return (DataSource) ctx.lookup("java:comp/env/jdbc/NLDI");
	}

	@Bean
	public String brokerURL() throws Exception {
		return (String) ctx.lookup("java:comp/env/jms/brokerURL");
	}

	@Bean
	public String queueName() throws Exception {
		return (String) ctx.lookup("java:comp/env/jms/nldiCrawler");
	}

}
