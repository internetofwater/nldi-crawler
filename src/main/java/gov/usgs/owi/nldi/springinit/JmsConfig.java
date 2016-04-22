package gov.usgs.owi.nldi.springinit;

import javax.jms.Session;
import javax.naming.NamingException;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import gov.usgs.owi.nldi.service.CrawlerMessageListener;

@Configuration
public class JmsConfig {

	@Autowired
	String brokerURL;
	@Autowired
	String queueName;
	@Autowired
	CrawlerMessageListener messageListener;

	@Bean
	public ActiveMQConnectionFactory connectionFactory() throws NamingException {
		ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory();
		amqFactory.setBrokerURL(brokerURL);
		return amqFactory;
	}

	@Bean
	public DefaultMessageListenerContainer mlc() throws NamingException {
		DefaultMessageListenerContainer mlc = new DefaultMessageListenerContainer();
		mlc.setConcurrentConsumers(1);
		mlc.setMaxConcurrentConsumers(1);
		mlc.setConnectionFactory(connectionFactory());
		mlc.setDestinationName(queueName);
		mlc.setMessageListener(messageListener);
		mlc.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
		return mlc;
	}

}
