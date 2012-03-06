package liquibase.ext.changewithnestedtags;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class SampleGrandChild implements CustomTaskChange {
    private String columnName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public SampleChild createGreatGrandChild() {
        return new SampleChild();
    }

	public String getConfirmationMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUp() throws SetupException {
		// TODO Auto-generated method stub
		
	}

	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// TODO Auto-generated method stub
		
	}

	public ValidationErrors validate(Database database) {
		// TODO Auto-generated method stub
		return null;
	}

	public void execute(Database database) throws CustomChangeException {
		// TODO Auto-generated method stub
		
	}
}
