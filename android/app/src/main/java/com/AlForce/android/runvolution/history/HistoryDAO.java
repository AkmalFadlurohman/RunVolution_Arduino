package com.AlForce.android.runvolution.history;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.AlForce.android.runvolution.utils.DatabaseAccessObject;
import com.AlForce.android.runvolution.utils.DatabaseOpenHelper;
import com.AlForce.android.runvolution.utils.DatabaseUpdateListener;

import java.util.Date;

import static com.AlForce.android.runvolution.utils.DatabaseOpenHelper.HISTORY_COLUMN_DATE;
import static com.AlForce.android.runvolution.utils.DatabaseOpenHelper.HISTORY_COLUMN_DISTANCE;
import static com.AlForce.android.runvolution.utils.DatabaseOpenHelper.HISTORY_COLUMN_ID;
import static com.AlForce.android.runvolution.utils.DatabaseOpenHelper.HISTORY_COLUMN_STEPS;
import static com.AlForce.android.runvolution.utils.DatabaseOpenHelper.HISTORY_TABLE_TABLENAME;

/**
 * Created by iqbal on 17/02/18.
 */

public class HistoryDAO implements DatabaseAccessObject<HistoryItem> {
    private static final String TAG = HistoryDAO.class.getSimpleName();
    private static final String HISTORY_TABLE = HISTORY_TABLE_TABLENAME;
    private static final String COLUMN_ID = HISTORY_COLUMN_ID;
    private static final String COLUMN_DATE = HISTORY_COLUMN_DATE;
    private static final String COLUMN_STEPS = HISTORY_COLUMN_STEPS;
    private static final String COLUMN_DISTANCE = HISTORY_COLUMN_DISTANCE;

    private DatabaseOpenHelper mDB;
    private SQLiteDatabase mReadableDB;
    private SQLiteDatabase mWritableDB;
    private DatabaseUpdateListener mListener;

    public HistoryDAO(DatabaseOpenHelper mDB) {
        this.mDB = mDB;
    }

    public HistoryDAO(DatabaseOpenHelper mDB, DatabaseUpdateListener mListener) {
        this.mDB = mDB;
        this.mListener = mListener;
    }

    public void setListener(DatabaseUpdateListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public HistoryItem query(int position) {
        String query = " SELECT * FROM " + HISTORY_TABLE +
                " ORDER BY " + COLUMN_DATE + " DESC " +
                "LIMIT " + position + ", 1";

        HistoryItem entry = new HistoryItem();

        mReadableDB = mDB.getReadableDB();
        try (Cursor cursor = mReadableDB.rawQuery(query, null)) {
            cursor.moveToFirst();
            entry = getHistoryItemFromCursor(cursor);
        } catch (Exception e) {
            Log.d(TAG, "query: " + e.getMessage());
        }

        return entry;
    }

    @Override
    public long insert(HistoryItem item) {
        long newId = 0;
        ContentValues values = historyItemToContentValues(item);

        mWritableDB = mDB.getWritableDB();
        try {
            newId = mWritableDB.insert(HISTORY_TABLE, null, values);
        } catch (Exception e) {
            Log.d(TAG, "insert: " + e.getMessage());
        }

        mListener.onDatabaseUpdate();
        return newId;
    }

    @Override
    public int delete(int id) {
        int deleted = 0;

        mWritableDB = mDB.getWritableDB();
        try {
            String whereClause = COLUMN_ID + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(id)};
            deleted = mWritableDB.delete(HISTORY_TABLE, whereClause, whereArgs);
        } catch (Exception e) {
            Log.d(TAG, "delete: " + e.getMessage());
        }

        mListener.onDatabaseUpdate();
        return deleted;
    }

    @Override
    public int update(HistoryItem item) {
        int mNumberOfRowsUpdated = -1;
        mWritableDB = mDB.getWritableDB();
        try {
            ContentValues values = historyItemToContentValues(item);
            String whereClause = COLUMN_ID + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(item.getId())};
            mNumberOfRowsUpdated = mWritableDB.update(HISTORY_TABLE,
                    values,
                    whereClause,
                    whereArgs);
        } catch (Exception e) {
            Log.d(TAG, "update: " + e.getMessage());
        }

        mListener.onDatabaseUpdate();
        return mNumberOfRowsUpdated;
    }

    @Override
    public long getQueryCount() {
        mReadableDB = mDB.getReadableDB();
        return DatabaseUtils.queryNumEntries(mReadableDB, HISTORY_TABLE);
    }

    private HistoryItem getHistoryItemFromCursor(Cursor cursor) {
        HistoryItem entry = new HistoryItem();
        int idIdx = cursor.getColumnIndex(COLUMN_ID);
        int dateIdx = cursor.getColumnIndex(COLUMN_DATE);
        int stepsIdx = cursor.getColumnIndex(COLUMN_STEPS);
        int distanceIdx = cursor.getColumnIndex(COLUMN_DISTANCE);

        entry.setId(cursor.getInt(idIdx));
        entry.setDate(new Date(cursor.getLong(dateIdx)));
        entry.setSteps(cursor.getInt(stepsIdx));
        entry.setDistance(cursor.getFloat(distanceIdx));
        return entry;
    }

    private ContentValues historyItemToContentValues(HistoryItem entry) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, entry.getDate().getTime());
        values.put(COLUMN_STEPS, entry.getSteps());
        values.put(COLUMN_DISTANCE, entry.getDistance());
        return values;
    }
}
