package gov.usgs.owi.nldi;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Statement;

@RunWith(SpringRunner.class)
@ActiveProfiles("it")
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		TransactionDbUnitTestExecutionListener.class
})
@DbUnitConfiguration(dataSetLoader = ColumnSensingFlatXMLDataSetLoader.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DatabaseTearDown("classpath:/cleanup/featureTables.xml")
public abstract class BaseIT extends BaseTest {

	protected void moveDataIntoTempTable(Statement statement, @NonNull String sourceSuffix) throws SQLException {
		statement.execute("truncate table nldi_data.feature_" + sourceSuffix + "_temp");
		statement.execute("with src_data as (select * from nldi_data.feature_" + sourceSuffix + ") insert into nldi_data.feature_" + sourceSuffix + "_temp select * from src_data");
		statement.execute("truncate table nldi_data.feature_" + sourceSuffix);
	}
}
