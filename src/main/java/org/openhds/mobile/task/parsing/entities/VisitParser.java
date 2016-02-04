package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Visits.
 */
public class VisitParser extends EntityParser<Visit> {

    private static final String pageName = "visit";

    @Override
    protected Visit toEntity(DataPage dataPage) {
        Visit visit = new Visit();
        visit.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        visit.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        visit.setLocationUuid(dataPage.getFirstString(asList(pageName, "location")));
        visit.setVisitDate(dataPage.getFirstString(asList(pageName, "visitDate")));
        visit.setFieldWorkerUuid(dataPage.getFirstString(asList(pageName, "collectedBy")));
        return visit;
    }
}
