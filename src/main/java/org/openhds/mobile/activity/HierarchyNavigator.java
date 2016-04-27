package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.model.form.FormBehavior;
import org.openhds.mobile.repository.DataWrapper;

public interface HierarchyNavigator {

	Map<String, Integer> getLevelLabels();

	List<String> getLevels();

	void jumpUp(String state);

	void stepDown(DataWrapper qr);

}
