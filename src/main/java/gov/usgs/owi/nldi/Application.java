package gov.usgs.owi.nldi;

import gov.usgs.owi.nldi.service.Ingestor;
import java.security.InvalidParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
  private Ingestor ingestor;

  @Autowired
  public Application(Ingestor ingestor) {
    this.ingestor = ingestor;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(String[] args) {
    if (args.length != 1) {
      throw new InvalidParameterException(
          "Invalid number of input parameters. Expected one integer parameter.");
    }
    Integer inputValue = Integer.parseInt(args[0]);

    ingestor.ingest(inputValue);
  }
}
