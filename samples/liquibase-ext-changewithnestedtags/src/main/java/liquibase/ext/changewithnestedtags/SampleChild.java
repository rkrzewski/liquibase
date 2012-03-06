package liquibase.ext.changewithnestedtags;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class SampleChild implements CustomTaskChange {
    private String name;
    private SampleGrandChild grandChild;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SampleGrandChild createFarNested() {
        grandChild = new SampleGrandChild();
        return grandChild;
    }

    public SampleGrandChild getGrandChild() {
        return grandChild;
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
