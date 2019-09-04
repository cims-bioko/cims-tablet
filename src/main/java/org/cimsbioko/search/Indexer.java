package org.cimsbioko.search;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import org.cimsbioko.App;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.cimsbioko.R;
import org.cimsbioko.utilities.NotificationUtils;

import java.io.File;
import java.io.IOException;

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.util.Version.LUCENE_47;
import static org.cimsbioko.App.getApp;
import static org.cimsbioko.navconfig.Hierarchy.*;
import static org.cimsbioko.provider.ContentProvider.getDatabaseHelper;
import static org.cimsbioko.utilities.NotificationUtils.*;
import static org.cimsbioko.utilities.SyncUtils.close;

public class Indexer {

    private static final String INDIVIDUAL_INDEX_QUERY = String.format("select %s, '%s' as level, %s," +
                    " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as name," +
                    " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as phone" +
                    " from %s", App.Individuals.COLUMN_INDIVIDUAL_UUID, INDIVIDUAL,
            App.Individuals.COLUMN_INDIVIDUAL_EXTID, App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME,
            App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME,
            App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
            App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, App.Individuals.TABLE_NAME);

    private static final String INDIVIDUAL_UPDATE_QUERY = String.format(INDIVIDUAL_INDEX_QUERY + " where %s = ?",
            App.Individuals.COLUMN_INDIVIDUAL_UUID);

    private static final String LOCATION_INDEX_QUERY = String.format("select %s, '%s' as level, %s, %s from %s",
            App.Locations.COLUMN_LOCATION_UUID, HOUSEHOLD, App.Locations.COLUMN_LOCATION_EXTID,
            App.Locations.COLUMN_LOCATION_NAME, App.Locations.TABLE_NAME);

    private static final String LOCATION_UPDATE_QUERY = String.format(LOCATION_INDEX_QUERY + " where %s = ?",
            App.Locations.COLUMN_LOCATION_UUID);

    private static final String HIERARCHY_INDEX_QUERY = String.format("select %s, %s as level, %s, %s from %s",
            App.HierarchyItems.COLUMN_HIERARCHY_UUID, App.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
            App.HierarchyItems.COLUMN_HIERARCHY_EXTID, App.HierarchyItems.COLUMN_HIERARCHY_NAME,
            App.HierarchyItems.TABLE_NAME);

    private static final String HIERARCHY_UPDATE_QUERY = String.format(HIERARCHY_INDEX_QUERY + " where %s = ?",
            App.HierarchyItems.COLUMN_HIERARCHY_UUID);

    private static final int NOTIFICATION_ID = 13;

    private static final String TAG = IndexingService.class.getSimpleName();

    private static Indexer instance;

    private File indexFile;
    private IndexWriter writer;

    private Indexer() {
        indexFile = new File(getApp().getApplicationContext().getFilesDir(), "search-index");
    }

    private SQLiteDatabase getDatabase() {
        return getDatabaseHelper(getApp().getApplicationContext()).getReadableDatabase();
    }

    public static Indexer getInstance() {
        if (instance == null) {
            instance = new Indexer();
        }
        return instance;
    }

    private IndexWriter getWriter(boolean reuse) throws IOException {
        if (writer != null && !reuse) {
            writer.close();
            writer = null;
        }
        if (writer == null) {
            Directory indexDir = FSDirectory.open(indexFile);
            Analyzer analyzer = new CustomAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
            config.setOpenMode(CREATE_OR_APPEND);
            writer = new IndexWriter(indexDir, config);
        }
        return writer;
    }

    void reindexAll() {
        try {
            IndexWriter indexWriter = getWriter(false);
            try {
                indexWriter.deleteAll();
                bulkIndexHierarchy(indexWriter);
                bulkIndexLocations(indexWriter);
                bulkIndexIndividuals(indexWriter);
            } finally {
                close(indexWriter);
            }
        } catch (IOException e) {
            Log.w(TAG, "io error, indexing failed: " + e.getMessage());
        }
    }

    private void bulkIndexHierarchy(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(HIERARCHY_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_hierarchy_items, new SimpleCursorDocumentSource(c), writer);
    }

    void reindexHierarchy(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(HIERARCHY_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new SimpleCursorDocumentSource(c), writer, App.HierarchyItems.COLUMN_HIERARCHY_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndexLocations(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(LOCATION_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_locations, new SimpleCursorDocumentSource(c), writer);
    }

    void reindexLocation(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(LOCATION_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new SimpleCursorDocumentSource(c), writer, App.Locations.COLUMN_LOCATION_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndexIndividuals(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(INDIVIDUAL_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_individuals, new IndividualCursorDocumentSource(c, "name", "phone"), writer);
    }

    void reindexIndividual(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(INDIVIDUAL_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new SimpleCursorDocumentSource(c), writer, App.Individuals.COLUMN_INDIVIDUAL_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndex(int label, DocumentSource source, IndexWriter writer) throws IOException {

        Context ctx = getApp().getApplicationContext();

        NotificationManager notificationManager = NotificationUtils.getNotificationManager(ctx);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setColor(getNotificationColor(ctx))
                .setContentTitle(ctx.getString(R.string.updating_index))
                .setContentText(ctx.getString(label))
                .setOngoing(true);

        try {
            if (source.next()) {
                int totalCount = source.size(), processed = 0, lastNotified = -1;
                do {
                    writer.addDocument(source.getDocument());
                    processed++;
                    int percentFinished = (int) ((processed / (float) totalCount) * 100);
                    if (lastNotified != percentFinished) {
                        notificationBuilder.setProgress(totalCount, processed, false);
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        lastNotified = percentFinished;
                    }
                } while (source.next());
            }
        } finally {
            notificationManager.cancel(NOTIFICATION_ID);
            source.close();
        }
    }

    private void updateIndex(DocumentSource source, IndexWriter writer, String idField) throws IOException {
        try {
            if (source.next()) {
                do {
                    Document doc = source.getDocument();
                    Term idTerm = new Term(idField, doc.get(idField));
                    writer.updateDocument(idTerm, doc);
                } while (source.next());
            }
        } finally {
            source.close();
        }
    }
}
