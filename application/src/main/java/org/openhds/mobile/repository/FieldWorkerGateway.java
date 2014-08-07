package org.openhds.mobile.repository;

import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FieldWorker;

import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    public FieldWorkerGateway() {
        super(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, COLUMN_FIELD_WORKER_EXTID, new FieldWorkerConverter());
    }

    private static class FieldWorkerConverter implements Converter<FieldWorker> {

        @Override
        public FieldWorker fromCursor(Cursor cursor) {
            FieldWorker fieldWorker = new FieldWorker();

            fieldWorker.setExtId(extractString(cursor, COLUMN_FIELD_WORKER_EXTID));
            fieldWorker.setFirstName(extractString(cursor, COLUMN_FIELD_WORKER_FIRST_NAME));
            fieldWorker.setLastName(extractString(cursor, COLUMN_FIELD_WORKER_LAST_NAME));
            fieldWorker.setPassword(extractString(cursor, COLUMN_FIELD_WORKER_PASSWORD));

            return fieldWorker;
        }

        @Override
        public ContentValues toContentValues(FieldWorker fieldWorker) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_FIELD_WORKER_EXTID, fieldWorker.getExtId());
            contentValues.put(COLUMN_FIELD_WORKER_FIRST_NAME, fieldWorker.getFirstName());
            contentValues.put(COLUMN_FIELD_WORKER_LAST_NAME, fieldWorker.getLastName());
            contentValues.put(COLUMN_FIELD_WORKER_PASSWORD, fieldWorker.getPassword());

            return contentValues;
        }

        @Override
        public String getId(FieldWorker fieldWorker) {
            return fieldWorker.getExtId();
        }
    }
}
