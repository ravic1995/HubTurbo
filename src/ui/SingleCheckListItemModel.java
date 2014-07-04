package ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SingleCheckListItemModel {
	
	private String contents;
	
	public SingleCheckListItemModel(String contents, boolean checked) {
		this.contents = contents;
		setChecked(checked);
	}
	
	public String getContents() {
		return contents;
	}
	
    private BooleanProperty checked = new SimpleBooleanProperty();
    public final Boolean getChecked() {
    	return checked.get();
    }
    public final void setChecked(Boolean value) {
    	checked.set(value);
    }
    public BooleanProperty checkedProperty() {
    	return checked;
    }

    @Override
    public String toString() {
    	return contents;
    }
    
}
