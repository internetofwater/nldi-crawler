package gov.usgs.owi.nldi.service;


import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

@Service
public class CrawlerMessageListener implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(CrawlerMessageListener.class);

	private Ingestor ingestor;

	@Autowired
	public CrawlerMessageListener(Ingestor ingestor) {
		this.ingestor = ingestor;
	}

	@Override
	public void onMessage(final Message message) {
		LOG.info("***** begin message ingest *****");
		long start = System.currentTimeMillis();
		String msgText = "";
		
		try {
			msgText = ((TextMessage)message).getText();
			ingestor.ingest(NumberUtils.parseNumber(msgText, Integer.class));
		} catch (NumberFormatException e) {
			LOG.error("Invalid ID given in the JMS Message:" + msgText);
		} catch (Exception e) {
			LOG.error("Something Bad Happened:", e);
		}
		
		LOG.info("***** end message ingest (" + (System.currentTimeMillis() - start) + " ms) *****");
	}

}
