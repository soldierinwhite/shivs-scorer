package io.github.soldierinwhite.cricketscorer.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by schoo on 2017/02/02.
 */

public final class ScoringContract {

    public static final String CONTENT_AUTHORITY = "io.github.soldierinwhite.cricketscorer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MATCHES = "matches";

    public static abstract class MatchEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MATCHES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of matches.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single match.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;

        public static final String TABLE_NAME = "matches";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TEAM_1 = "team_1";
        public static final String COLUMN_TEAM_2 = "team_2";
        public static final String COLUMN_OVER_LIMIT = "over_limit";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_MATCH_STATE = "match_state";
        public static final String COLUMN_SCORE_TEAM_1 = "score_team_1";
        public static final String COLUMN_SCORE_TEAM_2 = "score_team_2";
        public static final String COLUMN_WICKETS_LOST_TEAM_1 = "wickets_lost_team_1";
        public static final String COLUMN_WICKETS_LOST_TEAM_2 = "wickets_lost_team_2";
        public static final String COLUMN_BALLS_FACED_TEAM_1 = "balls_batted_last_over_team_1";
        public static final String COLUMN_BALLS_FACED_TEAM_2 = "balls_batted_last_over_team_2";
        public static final String COLUMN_TEAM_BATTING = "team_batting";

        //possible values of the state of the match
        public static final int IN_PROGRESS = 0;
        public static final int ABANDONED = 1;
        public static final int COMPLETED = 2;

        public static final int TEAM_1 = 0;
        public static final int TEAM_2 = 1;

        public static final int UNLIMITED_OVERS = -1;
    }
}
