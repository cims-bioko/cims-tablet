package org.openhds.mobile.projectdata.FormPayloadConsumers;

import java.util.Map;

import org.openhds.mobile.activity.HierarchyNavigatorActivity;

public interface FormPayloadConsumer {
	ConsumerResults consumeFormPayload(Map<String, String> formPayload,
									   HierarchyNavigatorActivity navigateActivity);
	void postFillFormPayload(Map<String, String> formPayload);
}