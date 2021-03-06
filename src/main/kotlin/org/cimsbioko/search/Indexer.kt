package org.cimsbioko.search

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.app.NotificationCompat
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.Term
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.navconfig.Hierarchy
import org.cimsbioko.provider.ContentProvider
import org.cimsbioko.utilities.NotificationUtils.PROGRESS_NOTIFICATION_RATE_MILLIS
import org.cimsbioko.utilities.NotificationUtils.SYNC_CHANNEL_ID
import org.cimsbioko.utilities.NotificationUtils.getNotificationColor
import org.cimsbioko.utilities.NotificationUtils.getNotificationManager
import org.cimsbioko.utilities.NotificationUtils.notificationIcon
import java.io.File
import java.io.IOException

class Indexer private constructor() {

    private val indexFile = File(App.instance.applicationContext.filesDir, "search-index")

    /* convenient access to the content provider's database, do not close the returned database! */
    private val database: SQLiteDatabase
        get() = ContentProvider.databaseHelper.readableDatabase

    @get:Throws(IOException::class)
    private val writer: IndexWriter
        get() {
            val indexDir: Directory = FSDirectory.open(indexFile)
            val analyzer: Analyzer = CustomAnalyzer()
            val config = IndexWriterConfig(Version.LUCENE_47, analyzer).apply {
                openMode = OpenMode.CREATE_OR_APPEND
            }
            return IndexWriter(indexDir, config)
        }

    fun reindexAll() {
        try {
            writer.use {
                with(it) {
                    deleteAll()
                    bulkIndexHierarchy()
                    bulkIndexLocations()
                    bulkIndexIndividuals()
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "io error, indexing failed: " + e.message)
        }
    }

    private fun reindexEntity(cursor: Cursor, idField: String) {
        writer.use {
            with(it) {
                try {
                    updateIndex(SimpleCursorDocumentSource(cursor), idField)
                } finally {
                    commit()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun reindexHierarchy(uuid: String) = reindexEntity(database.rawQuery(HIERARCHY_UPDATE_QUERY, arrayOf(uuid)), App.HierarchyItems.COLUMN_HIERARCHY_UUID)

    @Throws(IOException::class)
    fun reindexLocation(uuid: String) = reindexEntity(database.rawQuery(LOCATION_UPDATE_QUERY, arrayOf(uuid)), App.Locations.COLUMN_LOCATION_UUID)

    @Throws(IOException::class)
    fun reindexIndividual(uuid: String) = reindexEntity(database.rawQuery(INDIVIDUAL_UPDATE_QUERY, arrayOf(uuid)), App.Individuals.COLUMN_INDIVIDUAL_UUID)

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexHierarchy() {
        bulkIndex(R.string.indexing_hierarchy_items, SimpleCursorDocumentSource(database.rawQuery(HIERARCHY_INDEX_QUERY, emptyArray())))
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexLocations() {
        bulkIndex(R.string.indexing_locations, SimpleCursorDocumentSource(database.rawQuery(LOCATION_INDEX_QUERY, emptyArray())))
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndexIndividuals() {
        bulkIndex(R.string.indexing_individuals, IndividualCursorDocumentSource(database.rawQuery(INDIVIDUAL_INDEX_QUERY, arrayOf()), "name", "attrs"))
    }

    @Throws(IOException::class)
    private fun IndexWriter.bulkIndex(label: Int, source: DocumentSource) {
        val ctx = App.instance.applicationContext
        val notificationManager = getNotificationManager(ctx)
        val notificationBuilder = NotificationCompat.Builder(ctx, SYNC_CHANNEL_ID)
                .setSmallIcon(notificationIcon)
                .setColor(getNotificationColor(ctx))
                .setContentTitle(ctx.getString(R.string.updating_index))
                .setContentText(ctx.getString(label))
                .setOngoing(true)
        var lastUpdate: Long = 0
        source.use {
            with(it) {
                try {
                    if (next()) {
                        val totalCount = size()
                        var processed = 0
                        var lastNotified = -1
                        do {
                            addDocument(document)
                            processed++
                            val thisUpdate = System.currentTimeMillis()
                            if (thisUpdate - lastUpdate > PROGRESS_NOTIFICATION_RATE_MILLIS) {
                                val percentFinished = (processed / totalCount.toFloat() * 100).toInt()
                                if (lastNotified != percentFinished) {
                                    notificationBuilder.setProgress(totalCount, processed, false)
                                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                                    lastNotified = percentFinished
                                    lastUpdate = thisUpdate
                                }
                            }
                        } while (next())
                    }
                } finally {
                    notificationManager.cancel(NOTIFICATION_ID)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun IndexWriter.updateIndex(source: DocumentSource, idField: String) {
        source.use {
            with(it) {
                if (next()) {
                    do {
                        val doc = document
                        val idTerm = Term(idField, doc[idField])
                        updateDocument(idTerm, doc)
                    } while (next())
                }
            }
        }
    }

    companion object {
        private const val INDIVIDUAL_INDEX_QUERY = "select ${App.Individuals.COLUMN_INDIVIDUAL_UUID}, " +
                "'${Hierarchy.INDIVIDUAL}' as level, " +
                "${App.Individuals.COLUMN_INDIVIDUAL_EXTID}, " +
                "ifnull(${App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES},'') || ' ' || ifnull(${App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME},'') as name, " +
                "${App.Individuals.COLUMN_INDIVIDUAL_ATTRS} as attrs " +
                "from ${App.Individuals.TABLE_NAME}"
        private const val INDIVIDUAL_UPDATE_QUERY = "$INDIVIDUAL_INDEX_QUERY where ${App.Individuals.COLUMN_INDIVIDUAL_UUID} = ?"
        private const val LOCATION_INDEX_QUERY = "select ${App.Locations.COLUMN_LOCATION_UUID}, " +
                "'${Hierarchy.HOUSEHOLD}' as level, " +
                "${App.Locations.COLUMN_LOCATION_EXTID}, " +
                "${App.Locations.COLUMN_LOCATION_NAME} " +
                "from ${App.Locations.TABLE_NAME}"
        private const val LOCATION_UPDATE_QUERY = "$LOCATION_INDEX_QUERY where ${App.Locations.COLUMN_LOCATION_UUID} = ?"
        private const val HIERARCHY_INDEX_QUERY = "select ${App.HierarchyItems.COLUMN_HIERARCHY_UUID}, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_LEVEL} as level, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_EXTID}, " +
                "${App.HierarchyItems.COLUMN_HIERARCHY_NAME} " +
                "from ${App.HierarchyItems.TABLE_NAME}"
        private const val HIERARCHY_UPDATE_QUERY = "$HIERARCHY_INDEX_QUERY where ${App.HierarchyItems.COLUMN_HIERARCHY_UUID} = ?"
        private const val NOTIFICATION_ID = 13
        private val TAG = IndexingService::class.java.simpleName
        val instance by lazy { Indexer() }
    }
}