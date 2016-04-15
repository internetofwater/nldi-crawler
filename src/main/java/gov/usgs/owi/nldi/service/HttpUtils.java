package gov.usgs.owi.nldi.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gov.usgs.owi.nldi.domain.CrawlerSource;

@Component
public class HttpUtils {
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

	// 15 minutes, default is infinite
	private static int connection_ttl            = 15 * 60 * 1000;
	private static int connections_max_total     = 256;
	private static int connections_max_route     = 32;
	// 5 minutes, default is infinite
	private static int client_socket_timeout     = 5 * 60 * 1000;
    // 15 seconds, default is infinite
	private static int client_connection_timeout = 15 * 1000;

	public File callSourceSystem(CrawlerSource crawlerSource) throws ClientProtocolException, IOException {
		HttpEntity httpEntity = null;
		HttpResponse httpResponse;
		PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager(
				connection_ttl, TimeUnit.MILLISECONDS);
		clientConnectionManager.setMaxTotal(connections_max_total);
		clientConnectionManager.setDefaultMaxPerRoute(connections_max_route);

		RequestConfig config = RequestConfig.custom().setConnectTimeout(client_connection_timeout)
				.setSocketTimeout(client_socket_timeout).build();

		HttpClient httpClient = HttpClients.custom().setConnectionManager(clientConnectionManager)
				.setDefaultRequestConfig(config).build();
		
		HttpContext localContext = new BasicHttpContext();
		httpResponse = httpClient.execute(new HttpGet(crawlerSource.getSourceUri()), localContext);
		
		httpEntity = httpResponse.getEntity();
		return buildFile(httpEntity, crawlerSource);

	}
	
	protected File buildFile(HttpEntity httpEntity, CrawlerSource crawlerSource) {
        File file = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
		try {
			file = File.createTempFile(crawlerSource.getTableName(), "geojson");
			LOG.trace("Creating file:" + file.getPath() + file.getName());
			bis = new BufferedInputStream(httpEntity.getContent());
			bos = new BufferedOutputStream(new FileOutputStream(file));
		   	byte[] buff = new byte[1024*8];
		   	int count=0;
			while((count = bis.read(buff)) != -1) {
				bos.write(buff,0,count);
			}
		} catch (Exception e) {
			LOG.error("Error saving geojson:", e);
		} finally {
			try {
                // This is important to guarantee connection release back into
                // connection pool for future reuse!
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                LOG.error("Consuming remaining bytes in server response entity:", e);
            }
        	
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				String msg = "Exception closing buffered streams [" + e.getMessage() + "] continuing...";
				LOG.error(msg);
			}
         }
		return file;
	}
}
