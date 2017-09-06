package io.github.soldierinwhite.cricketscorer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.ProgressBar;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;

/**
 * Created by schoo on 2017/02/06.
 */

public class MatchSelector extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MatchAdapter.MatchAdapterOnClickHandler{

    private MatchAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;

    private int mPosition = RecyclerView.NO_POSITION;

    public static final String[] MATCHES_PROJECTION = {
            ScoringContract.MatchEntry.COLUMN_TEAM_1,
            ScoringContract.MatchEntry.COLUMN_TEAM_2,
            ScoringContract.MatchEntry.COLUMN_MATCH_STATE,
            ScoringContract.MatchEntry.COLUMN_DATE,
            ScoringContract.MatchEntry.COLUMN_OVER_LIMIT,
            ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1,
            ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2,
            ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1,
            ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2,
            ScoringContract.MatchEntry._ID,
            ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1,
            ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2,
            ScoringContract.MatchEntry.COLUMN_TEAM_BATTING
    };

    public static final int INDEX_TEAM_1 = 0;
    public static final int INDEX_TEAM_2 = 1;
    public static final int INDEX_MATCH_STATE = 2;
    public static final int INDEX_DATE = 3;
    public static final int INDEX_OVER_LIMIT = 4;
    public static final int INDEX_SCORE_TEAM_1 = 5;
    public static final int INDEX_SCORE_TEAM_2 = 6;
    public static final int INDEX_WICKETS_LOST_TEAM_1 = 7;
    public static final int INDEX_WICKETS_LOST_TEAM_2 = 8;
    public static final int INDEX_ID = 9;
    public static final int INDEX_BALLS_FACED_TEAM_1 = 10;
    public static final int INDEX_BALLS_FACED_TEAM_2 = 11;
    public static final int INDEX_TEAM_BATTING = 12;

    public static final int ID_MATCH_LOADER = 111;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_matches);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_spinner_matches);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MatchAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(ID_MATCH_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = Long.parseLong(viewHolder.itemView.getTag().toString());
                removeMatch(id);
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                refreshList();
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case ID_MATCH_LOADER:
                Uri matchesQueryUri = ScoringContract.MatchEntry.CONTENT_URI;
                String sortOrder = ScoringContract.MatchEntry.COLUMN_DATE + " ASC";

                return new CursorLoader(this,
                        matchesQueryUri,
                        MATCHES_PROJECTION,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if(mPosition == RecyclerView.NO_POSITION){
            mPosition = 0;
        }
        mRecyclerView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(Bundle data) {
        Intent scoringIntent = new Intent(MatchSelector.this, ScorerActivity.class);

        String uriAtId = ScoringContract.MatchEntry.CONTENT_URI.toString()+ "/" + data.getLong(ScoringContract.MatchEntry._ID);

        scoringIntent.putExtra(getString(R.string.match_uri_extra), uriAtId);
        scoringIntent.putExtra(getString(R.string.new_match_boolean_extra), false);
        scoringIntent.putExtra(getString(R.string.match_data_bundle), data);

        startActivity(scoringIntent);
    }

    private void removeMatch(long id){
        String [] whereArgs = {String.valueOf(id)};
        getContentResolver().delete(ScoringContract.MatchEntry.CONTENT_URI,
                ScoringContract.MatchEntry._ID + "=?",
                whereArgs);
    }

    private void refreshList(){
        getSupportLoaderManager().restartLoader(ID_MATCH_LOADER, null, this);
    }
}
