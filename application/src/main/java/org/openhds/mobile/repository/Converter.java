package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    public T fromCursor(Cursor cursor);

    public ContentValues toContentValues(T entity);

    public String getId(T entity);

    public QueryResult toQueryResult(ContentResolver contentResolver, T entity, String state);

}
