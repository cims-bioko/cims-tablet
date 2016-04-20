package org.openhds.mobile.model.form;

import org.openhds.mobile.projectdata.FormFilters.FormFilter;
import org.openhds.mobile.projectdata.FormPayloadBuilders.FormPayloadBuilder;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.util.ArrayList;

import static org.openhds.mobile.projectdata.ProjectActivityBuilder.getString;

public class FormBehavior {

    private String formName;
    private String labelKey;
    private FormFilter formFilter;
    private FormPayloadBuilder formPayloadBuilder;
    private FormPayloadConsumer formPayloadConsumer;

    // ArrayList, not just List, because of user with Android Parcelable interface.
    private ArrayList<FormSearchPluginModule> formSearchPluginModules;

    public FormBehavior(String formName,
                        String labelKey,
                        FormFilter formFilter,
                        FormPayloadBuilder formMapper,
                        FormPayloadConsumer formPayloadConsumer) {
        this(formName, labelKey, formFilter, formMapper, formPayloadConsumer, null);
    }

    public FormBehavior(String formName,
                        String labelKey,
                        FormFilter formFilter,
                        FormPayloadBuilder formMapper,
                        FormPayloadConsumer formPayloadConsumer,
                        ArrayList<FormSearchPluginModule> formSearchPluginModules) {

        this.formName = formName;
        this.labelKey = labelKey;
        this.formFilter = formFilter;
        this.formPayloadBuilder = formMapper;
        this.formPayloadConsumer = formPayloadConsumer;
        this.formSearchPluginModules = formSearchPluginModules;
    }

    public String getFormName() {
        return formName;
    }

    public String getLabel() {
        return getString(labelKey);
    }

    public FormFilter getFormFilter() {
        return formFilter;
    }

    public FormPayloadBuilder getFormPayloadBuilder() {
        return formPayloadBuilder;
    }

    public FormPayloadConsumer getFormPayloadConsumer() {
        return formPayloadConsumer;
    }

    public ArrayList<FormSearchPluginModule> getFormSearchPluginModules() {
        return formSearchPluginModules;
    }

    public boolean getNeedsFormFieldSearch() {
        return null != formSearchPluginModules && formSearchPluginModules.size() > 0;
    }
}