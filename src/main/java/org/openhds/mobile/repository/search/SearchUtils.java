package org.openhds.mobile.repository.search;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;

import static org.openhds.mobile.repository.GatewayRegistry.getFieldWorkerGateway;
import static org.openhds.mobile.repository.GatewayRegistry.getIndividualGateway;
import static org.openhds.mobile.repository.GatewayRegistry.getLocationGateway;

/**
 * Utility methods for working with Gateways, SearchFragment
 * and EntitySearchActivity.
 *
 * For example, get pre-configured FormSearchPluginModules for
 * each gateway with "typical" labels and columns to search.
 */
public class SearchUtils {

    // Search for a field worker based on name and id.
    public static EntityFieldSearch getFieldWorkerModule(String fieldName) {
        EntityFieldSearch plugin = new EntityFieldSearch(getFieldWorkerGateway(), R.string.search_field_worker_label, fieldName);
        plugin.getColumnsAndLabels().put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, R.string.field_worker_first_name_label);
        plugin.getColumnsAndLabels().put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, R.string.field_worker_last_name_label);
        plugin.getColumnsAndLabels().put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, R.string.field_worker_id_label);
        return plugin;
    }

    // Search for an individual based name and phone number.
    public static EntityFieldSearch getIndividualModule(String fieldName, int searchLabel) {
        EntityFieldSearch plugin = new EntityFieldSearch(getIndividualGateway(), searchLabel, fieldName);
        plugin.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, R.string.individual_first_name_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, R.string.individual_last_name_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, R.string.individual_personal_phone_number_label);
        return plugin;
    }

    public static EntityFieldSearch getIndividualModule(String fieldName) {
        return getIndividualModule(fieldName, R.string.search_individual_label);
    }

    // Search for a location based on name, id, and location hierarchy names.
    public static EntityFieldSearch getLocationModule(String fieldName) {
        EntityFieldSearch plugin = new EntityFieldSearch(getLocationGateway(), R.string.search_location_label, fieldName);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_NAME, R.string.location_name_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, R.string.location_ext_id_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, R.string.location_sector_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, R.string.location_map_area_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME, R.string.location_locality_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME, R.string.location_community_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE, R.string.location_community_code_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER, R.string.location_building_number_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER, R.string.location_floor_number_label);
        return plugin;
    }
}
