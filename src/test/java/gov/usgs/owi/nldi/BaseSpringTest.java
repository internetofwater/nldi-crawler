package gov.usgs.owi.nldi;

import java.io.IOException;

import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.FileCopyUtils;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import gov.usgs.owi.nldi.springinit.SpringConfig;
import gov.usgs.owi.nldi.springinit.TestSpringConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringConfig.class, TestSpringConfig.class})
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, 
	TransactionDbUnitTestExecutionListener.class
	})
@DbUnitConfiguration(dataSetLoader = ColumnSensingFlatXMLDataSetLoader.class)
public abstract class BaseSpringTest {

	public String getSourceFile(String file) throws IOException {
		return new String(FileCopyUtils.copyToByteArray(new ClassPathResource("testData/" + file).getInputStream()));
	}

	public String getCompareFile(String file) throws IOException {
		return new String(FileCopyUtils.copyToByteArray(new ClassPathResource("testResult/" + file).getInputStream()));
	}

}
