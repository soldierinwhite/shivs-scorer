package io.github.soldierinwhite.cricketscorer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract.MatchEntry;

/**
 * Created by schoo on 2017/02/02.
 */

public class ScoringDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ScoringDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "CricketScorer.db";

    public ScoringDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " +
                MatchEntry.TABLE_NAME + " (" +
                MatchEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MatchEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                MatchEntry.COLUMN_TEAM_1 + " TEXT NOT NULL, " +
                MatchEntry.COLUMN_TEAM_2 + " TEXT NOT NULL, " +
                MatchEntry.COLUMN_OVER_LIMIT + " INTEGER, " +
                MatchEntry.COLUMN_MATCH_STATE + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_SCORE_TEAM_1 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_WICKETS_LOST_TEAM_1 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_BALLS_FACED_TEAM_1 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_SCORE_TEAM_2 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_WICKETS_LOST_TEAM_2 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_BALLS_FACED_TEAM_2 + " INTEGER NOT NULL DEFAULT 0, " +
                MatchEntry.COLUMN_TEAM_BATTING + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
