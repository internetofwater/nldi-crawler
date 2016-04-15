package gov.usgs.owi.nldi.service;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.usgs.owi.nldi.BaseSpringTest;

public class CrawlerMessageListenerTest extends BaseSpringTest {

	@Mock
	private Ingestor ingestor;
	@Mock
	private TextMessage mockMessage;
	
	private CrawlerMessageListener crawlerMessageListener;
	
	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
		crawlerMessageListener = new CrawlerMessageListener(ingestor);
	}

	@Test
	public void onMessageTest() throws JMSException, ClientProtocolException, IOException {
		when(mockMessage.getText()).thenReturn("1", "a").thenThrow(new JMSException("ouch"));
		
		crawlerMessageListener.onMessage(mockMessage);
		verify(ingestor).ingest(anyInt());
		
		crawlerMessageListener.onMessage(mockMessage);
		//Still 1 time as we couldn't convert an "a" to an int.
		verify(ingestor).ingest(anyInt());
		
		crawlerMessageListener.onMessage(mockMessage);
		//Still 1 time as we got a JMSException.
		verify(ingestor).ingest(anyInt());
	}
	
}
