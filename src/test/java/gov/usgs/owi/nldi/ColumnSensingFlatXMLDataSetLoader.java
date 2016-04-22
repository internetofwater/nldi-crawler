package gov.usgs.owi.nldi;

import java.io.InputStream;
import java.sql.Date;
import java.time.Instant;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.core.io.Resource;

import com.github.springtestdbunit.dataset.AbstractDataSetLoader;

public class ColumnSensingFlatXMLDataSetLoader extends AbstractDataSetLoader {

	@Override
	protected IDataSet createDataSet(Resource resource) throws Exception {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		builder.setColumnSensing(true);
		try (InputStream inputStream = resource.getInputStream()) {
			return createReplacementDataSet(builder.build(inputStream));
		}
	}

	private ReplacementDataSet createReplacementDataSet(FlatXmlDataSet dataSet) {
		ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
		replacementDataSet.addReplacementObject("[today]", Date.from(Instant.now()));
		replacementDataSet.addReplacementObject("[NULL]", null);
		return replacementDataSet;
	}

}
