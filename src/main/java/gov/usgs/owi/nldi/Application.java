
package gov.usgs.owi.nldi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.NumberUtils;

import gov.usgs.owi.nldi.service.Ingestor;

@SpringBootApplication
public class Application implements CommandLineRunner {
	private Ingestor ingestor;

	@Autowired
	public Application(Ingestor ingestor){
		this.ingestor = ingestor;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String[] args) throws Exception {
		ingestor.ingest(NumberUtils.parseNumber(args[0], Integer.class));
	}
}
