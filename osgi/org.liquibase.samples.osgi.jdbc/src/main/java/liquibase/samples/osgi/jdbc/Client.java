package liquibase.samples.osgi.jdbc;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class Client {

	private DataSource dataSource;

	@Reference
	protected void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Activate
	protected void activate() throws SQLException {
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt
				.executeQuery("select count(*) from fruit where color='green'");
		assert rset.isBeforeFirst();
		rset.next();
		System.out.println(format("found %d green fruit", rset.getInt(1)));
		rset.close();
		stmt.close();
		conn.close();
	}
}
