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
public class JdbcTest {

	private DataSource dataSource;

	@Reference(target = "(liquibase.updated=true)")
	protected void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Activate
	protected void activate() throws SQLException {
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt
				.executeQuery("select id, name, active from department");
		int count = 0;
		while (rset.next()) {
			System.out.println(format("department %d %s %s", rset.getInt(1), rset
					.getString(2), rset.getBoolean(3) ? "active" : "inactive"));
			count++;
		}
		System.out.println(format("found %d record(s)", count));
		rset.close();
		stmt.close();
		conn.close();
	}
}
