package io.github.soldierinwhite.cricketscorer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.TimeZone;

/**
 * Created by schoo on 2017/02/02.
 */

public class ScoringProvider extends ContentProvider {

    public static final String LOG_TAG = ScoringProvider.class.getSimpleName();

    /*
 * These constant will be used to match URIs with the data they are looking for. We will take
 * advantage of the UriMatcher class to make that matching MUCH easier than doing something
 * ourselves, such as using regular expressions.
 */

    //Don't confuse the two uses of "match", in the codes, it refers to the cricket matches in the database
    //whereas in the UriMatcher it refers to whether the incoming uri matches one that is prepared for
    //by the provider.

    public static final int CODE_MATCHES = 100;
    public static final int CODE_MATCHES_WITH_ID = 101;

    /*
 * The URI Matcher used by this content provider.
 */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private ScoringDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScoringContract.CONTENT_AUTHORITY;

        /* This URI is content://io.github.soldierinwhite.cricketscorer/matches/ */
        matcher.addURI(authority, ScoringContract.PATH_MATCHES, CODE_MATCHES);

        /* This URI is content://io.github.soldierinwhite.cricketscorer/matches/#/ */
        matcher.addURI(authority, ScoringContract.PATH_MATCHES + "/#", CODE_MATCHES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScoringDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);

        switch (match){
            case CODE_MATCHES:
                cursor = db.query(ScoringContract.MatchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MATCHES_WITH_ID:
                selection = ScoringContract.MatchEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ScoringContract.MatchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the Cursor,
        //so we know what content URI the Cursor was created for
        //If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case CODE_MATCHES:
                return ScoringContract.MatchEntry.CONTENT_LIST_TYPE;
            case CODE_MATCHES_WITH_ID:
                return ScoringContract.MatchEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case CODE_MATCHES:
                return insertMatch(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);
        }
    }

    private Uri insertMatch(Uri uri, ContentValues contentValues){
        long dateInMillis = getNormalizedUtcDateForToday();
        contentValues.put(ScoringContract.MatchEntry.COLUMN_DATE, dateInMillis);
        String team1 = contentValues.getAsString(ScoringContract.MatchEntry.COLUMN_TEAM_1);
        if(team1 == null || team1.isEmpty()){
            throw new IllegalArgumentException("Team 1 requires a name");
        }
        String team2 = contentValues.getAsString(ScoringContract.MatchEntry.COLUMN_TEAM_2);
        if(team2 == null || team2.isEmpty()){
            throw new IllegalArgumentException("Team 2 requires a name");
        }
        Integer overLimit = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT);
        if(overLimit < 1 || overLimit > 100){
            throw new IllegalArgumentException("Over limit must be between 1 and 100 or blank");
        }

        Integer matchState = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_MATCH_STATE);
        if(matchState < 0 || matchState > 2 || matchState == null ){
            throw new IllegalArgumentException("Invalid match state");
        }

        Integer team1score = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1);
        if(team1score < 0 || team1score == null){
            throw new IllegalArgumentException("Invalid score for team 1");
        }

        Integer team2score = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2);
        if(team2score < 0 || team2score == null){
            throw new IllegalArgumentException("Invalid score for team 2");
        }

        Integer team1wickets = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1);
        if(team1wickets < 0 || team1wickets > 10 || team1wickets == null){
            throw new IllegalArgumentException("Invalid wicket loss for team 1");
        }

        Integer team2wickets = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2);
        if(team2wickets < 0 || team2wickets > 10 || team2wickets == null){
            throw new IllegalArgumentException("Invalid wicket loss for team 2");
        }

        Integer team1ballsBatted = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1);
        if(team1ballsBatted < 0 || team1ballsBatted == null){
            throw new IllegalArgumentException("Invalid number of overs batted for team 1");
        }

        Integer team2ballsBatted = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2);
        if(team2ballsBatted < 0 || team2ballsBatted == null){
            throw new IllegalArgumentException("Invalid number of overs batted for team 2");
        }

        Integer teamBatting = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING);
        if(teamBatting < 0 || teamBatting > 1 || teamBatting == null){
            throw new IllegalArgumentException("Invalid team batting");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long id = db.insert(ScoringContract.MatchEntry.TABLE_NAME, null, contentValues);

        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch(match){
            case CODE_MATCHES:
                rowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(ScoringContract.MatchEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case CODE_MATCHES_WITH_ID:
                selection = ScoringContract.MatchEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(ScoringContract.MatchEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Delete is not supported for uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case CODE_MATCHES_WITH_ID:
                selection = ScoringContract.MatchEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMatch(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not suppported for uri: " + uri);
        }
    }

    private int updateMatch(Uri uri, ContentValues contentValues, String selection, String [] selectionArgs){
        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_TEAM_1)) {
            String team1 = contentValues.getAsString(ScoringContract.MatchEntry.COLUMN_TEAM_1);
            if (team1 == null || team1.isEmpty()) {
                throw new IllegalArgumentException("Team 1 requires a name");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_TEAM_2)) {
            String team2 = contentValues.getAsString(ScoringContract.MatchEntry.COLUMN_TEAM_2);
            if (team2 == null || team2.isEmpty()) {
                throw new IllegalArgumentException("Team 2 requires a name");
            }
        }
        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT)) {
            Integer overLimit = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT);
            if (overLimit < 1 || overLimit > 100) {
                throw new IllegalArgumentException("Over limit must be between 1 and 100 or blank");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_MATCH_STATE)) {
            Integer matchState = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_MATCH_STATE);
            if (matchState < 0 || matchState > 2 || matchState == null) {
                throw new IllegalArgumentException("Invalid match state");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1)) {
            Integer team1score = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1);
            if (team1score < 0 || team1score == null) {
                throw new IllegalArgumentException("Invalid score for team 1");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2)) {
            Integer team2score = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2);
            if (team2score < 0 || team2score == null) {
                throw new IllegalArgumentException("Invalid score for team 2");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1)) {
            Integer team1wickets = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1);
            if (team1wickets < 0 || team1wickets > 10 || team1wickets == null) {
                throw new IllegalArgumentException("Invalid wicket loss for team 1");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2)) {
            Integer team2wickets = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2);
            if (team2wickets < 0 || team2wickets > 10 || team2wickets == null) {
                throw new IllegalArgumentException("Invalid wicket loss for team 2");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1)) {
            Integer team1ballsBatted = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1);
            if (team1ballsBatted < 0 || team1ballsBatted == null) {
                throw new IllegalArgumentException("Invalid number of overs batted for team 1");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2)) {
            Integer team2ballsBatted = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2);
            if (team2ballsBatted < 0 || team2ballsBatted == null) {
                throw new IllegalArgumentException("Invalid number of overs batted for team 2");
            }
        }

        if(contentValues.containsKey(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING)) {
            Integer teamBatting = contentValues.getAsInteger(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING);
            if (teamBatting < 0 || teamBatting > 1 || teamBatting == null) {
                throw new IllegalArgumentException("Invalid team batting");
            }
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsUpdated = db.update(ScoringContract.MatchEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public static long getNormalizedUtcDateForToday() {
        long utcNowMillis = System.currentTimeMillis();
        TimeZone currentTimeZone = TimeZone.getDefault();
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);
        long timeSinceEpochLocaltimeMillis = utcNowMillis + gmtOffsetMillis;
        return timeSinceEpochLocaltimeMillis;

    }
}
