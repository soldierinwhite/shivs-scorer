package io.github.soldierinwhite.cricketscorer;


import android.support.v4.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;
import io.github.soldierinwhite.cricketscorer.databinding.ActivityScorerBinding;
import io.github.soldierinwhite.cricketscorer.util.DisplayUtils;

/**
 * Created by schoo on 2017/02/02.
 */
public class ScorerActivity extends AppCompatActivity implements MiscButtonsFragment.MiscFragmentInteractionListener{

    ActivityScorerBinding mBinding;

    private final String LOG_TAG = ScorerActivity.class.getSimpleName();

    private final int BALLS_PER_OVER = 6;
    private final int PENALTY_RUNS = 5;

    private Bundle mMatchData;
    private Uri mMatchUri;

    private int mCurrentRuns;
    private int mCurrentWickets;
    private int mCurrentBallsBowled;
    private int mTeamBatting;
    private int mOverLimit;
    private int mTarget;
    private int numPenaltiesAwardedToTeamThatHasNotBatted = 0;
    private int runsScoredThisBall;

    private String mCurrentTeam;
    private String mScore;
    private String mOversBowled;

    private static final int SIMPLE_SCORE_UPDATE_ID = 1;
    private static final int INNINGS_CHANGE_UPDATE_ID = 2;
    private static final int MATCH_STATE_COMPLETED_UPDATE_ID = 3;
    private static final int MATCH_STATE_ABANDONED_UPDATE_ID = 4;
    private static final int PENALTY_AWARDED_TO_TEAM_NOT_BATTING_ID = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorer);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scorer);

        mMatchUri = Uri.parse(getIntent().getStringExtra(getString(R.string.match_uri_extra)));
        Log.d(LOG_TAG, "mMatchUri = " + mMatchUri.toString());
        mMatchData = getIntent().getBundleExtra(getString(R.string.match_data_bundle));

        mOverLimit = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT);
        if (!getIntent().getBooleanExtra(getString(R.string.new_match_boolean_extra), false)) {
            mTeamBatting = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING);
            switch (mTeamBatting) {
                case ScoringContract.MatchEntry.TEAM_1:
                    mCurrentTeam = mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_1);
                    mCurrentRuns = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1);
                    mCurrentWickets = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1);
                    mCurrentBallsBowled = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1);
                    numPenaltiesAwardedToTeamThatHasNotBatted = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2)/PENALTY_RUNS;
                    break;
                case ScoringContract.MatchEntry.TEAM_2:
                    mCurrentTeam = mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_2);
                    mCurrentRuns = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2);
                    mCurrentWickets = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2);
                    mCurrentBallsBowled = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2);
                    mTarget = mMatchData.getInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown batting team index: " + mTeamBatting);
            }
        } else {
            mTeamBatting = ScoringContract.MatchEntry.TEAM_1;
            mCurrentTeam = mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_1);
            mCurrentRuns = 0;
            mCurrentRuns = 0;
            mCurrentBallsBowled = 0;
        }

        setTeamName();
        setScoreDisplay();
        setOversDisplay();
        setTargetDisplay();
    }

    private void setScoreDisplay() {
        checkWin();
        mScore = mCurrentRuns + "/" + mCurrentWickets;
        mBinding.scoreTextView.setText(mScore);
    }

    private void setTargetDisplay() {
        if(mTeamBatting == ScoringContract.MatchEntry.TEAM_2){
            mBinding.targetTextView.setVisibility(View.VISIBLE);
            mBinding.targetTextView.setText(DisplayUtils.setTarget(mTarget));
        } else {
            mBinding.targetTextView.setVisibility(View.GONE);
        }
    }

    private void setOversDisplay() {
        int fullOvers = mCurrentBallsBowled / BALLS_PER_OVER;
        if (fullOvers >= mOverLimit) {
            if (mTeamBatting == ScoringContract.MatchEntry.TEAM_1) {
                inningsChange();
            } else {
                completeMatch();
            }
        }
        int ballsBowledThisOver = mCurrentBallsBowled % BALLS_PER_OVER;
        if (ballsBowledThisOver == 0) {
            mOversBowled = String.valueOf(fullOvers);
        } else {
            mOversBowled = fullOvers + "." + ballsBowledThisOver;
        }
        mBinding.overCount.setText("Overs: " + mOversBowled);
    }

    private void setTeamName() {
        mBinding.teamAbbreviation.setText(DisplayUtils.teamNameAbbreviate(mCurrentTeam));
    }

    public void dotBall(View view) {
        mCurrentBallsBowled++;
        runsScoredThisBall = 0;
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void oneRun(View view) {
        mCurrentRuns++;
        mCurrentBallsBowled++;
        runsScoredThisBall = 1;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void twoRuns(View view) {
        mCurrentRuns += 2;
        mCurrentBallsBowled++;
        runsScoredThisBall = 2;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void threeRuns(View view) {
        mCurrentRuns += 3;
        mCurrentBallsBowled++;
        runsScoredThisBall = 3;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void fourRuns(View view) {
        mCurrentRuns += 4;
        mCurrentBallsBowled++;
        runsScoredThisBall = 4;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void sixRuns(View view) {
        mCurrentRuns += 6;
        mCurrentBallsBowled++;
        runsScoredThisBall = 5;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void wide(View view) {
        mCurrentRuns++;
        setScoreDisplay();
        runsScoredThisBall = 0;
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void noBall(View view) {
        mCurrentRuns++;
        setScoreDisplay();
        runsScoredThisBall = 0;
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void bye(View view) {
        mCurrentRuns++;
        mCurrentBallsBowled++;
        runsScoredThisBall = 1;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void legBye(View view) {
        mCurrentRuns++;
        mCurrentBallsBowled++;
        runsScoredThisBall = 1;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    public void wicket(View view) {
        mCurrentBallsBowled++;
        mCurrentWickets++;
        runsScoredThisBall = 0;
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
        if(mCurrentWickets >= 10){
            if(mTeamBatting == ScoringContract.MatchEntry.TEAM_1){
                inningsChange();
            } else {
                completeMatch();
            }
        }
    }

    public void miscFragment(View view) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.misc_options_frame_layout,
                MiscButtonsFragment.newInstance(
                        mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_1),
                        mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_2)));
        ft.addToBackStack(null);
        ft.commit();
    }

    private void simpleScoreUpdate() {
        Log.d(LOG_TAG, "In simpleScoreUpdate...");
        ContentValues values = new ContentValues();
        Log.d(LOG_TAG, "mTeamBatting = " + mTeamBatting);
        if (mTeamBatting == ScoringContract.MatchEntry.TEAM_1) {
            values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1, mCurrentRuns);
            values.put(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1, mCurrentWickets);
            values.put(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1, mCurrentBallsBowled);
            Log.d(LOG_TAG, "Added contentvalues for Team 1");
        } else {
            values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2, mCurrentRuns);
            values.put(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2, mCurrentWickets);
            values.put(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2, mCurrentBallsBowled);
        }
        Log.d(LOG_TAG, "Proceed to update");
        int rowsUpdated = getContentResolver().update(mMatchUri, values, null, null);
        if (rowsUpdated == 0) Log.e(LOG_TAG, "Score update failed.");
    }

    private void inningsChangeUpdate() {
        ContentValues values = new ContentValues();
        values.put(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING, mTeamBatting);
        int rowsUpdated = getContentResolver().update(mMatchUri, values, null, null);
        if (rowsUpdated == 0) Log.e(LOG_TAG, "Innings change update failed.");
    }

    private void matchStateUpdate(int newMatchState) {
        ContentValues values = new ContentValues();
        int rowsUpdated;
        switch (newMatchState) {
            case ScoringContract.MatchEntry.COMPLETED:
                values.put(ScoringContract.MatchEntry.COLUMN_MATCH_STATE, ScoringContract.MatchEntry.COMPLETED);
                rowsUpdated = getContentResolver().update(mMatchUri, values, null, null);
                break;
            case ScoringContract.MatchEntry.ABANDONED:
                values.put(ScoringContract.MatchEntry.COLUMN_MATCH_STATE, ScoringContract.MatchEntry.ABANDONED);
                rowsUpdated = getContentResolver().update(mMatchUri, values, null, null);
                break;
            default:
                throw new IllegalArgumentException("Invalid new match state: " + newMatchState);
        }
        if (rowsUpdated == 0) Log.e(LOG_TAG, "Match completed update failed.");
    }

    private void update(int updateId) {
        Log.d(LOG_TAG, "Update called");
        switch (updateId) {
            case SIMPLE_SCORE_UPDATE_ID:
                Log.d(LOG_TAG, "Score update called");
                simpleScoreUpdate();
                break;
            case INNINGS_CHANGE_UPDATE_ID:
                inningsChangeUpdate();
                break;
            case MATCH_STATE_COMPLETED_UPDATE_ID:
                matchStateUpdate(ScoringContract.MatchEntry.COMPLETED);
                break;
            case MATCH_STATE_ABANDONED_UPDATE_ID:
                matchStateUpdate(ScoringContract.MatchEntry.ABANDONED);
                break;
            case PENALTY_AWARDED_TO_TEAM_NOT_BATTING_ID:
                ContentValues values = new ContentValues();
                if(mTeamBatting == ScoringContract.MatchEntry.TEAM_1){
                    numPenaltiesAwardedToTeamThatHasNotBatted++;
                    values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2, PENALTY_RUNS * numPenaltiesAwardedToTeamThatHasNotBatted);
                    getContentResolver().update(mMatchUri, values, null, null);
                } else {
                    mTarget+=PENALTY_RUNS;
                    values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1, mTarget);
                    getContentResolver().update(mMatchUri, values, null, null);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown update identification number: " + updateId);

        }
    }

    private void inningsChange() {
        update(SIMPLE_SCORE_UPDATE_ID);
        mTeamBatting = ScoringContract.MatchEntry.TEAM_2;
        mTarget = mCurrentRuns + 1;
        update(INNINGS_CHANGE_UPDATE_ID);
        mCurrentBallsBowled = 0;
        mCurrentRuns = PENALTY_RUNS * numPenaltiesAwardedToTeamThatHasNotBatted;
        mCurrentWickets = 0;
        mCurrentTeam = mMatchData.getString(ScoringContract.MatchEntry.COLUMN_TEAM_2);
        setTeamName();
        setScoreDisplay();
        setOversDisplay();
        update(SIMPLE_SCORE_UPDATE_ID);
    }

    private void completeMatch() {
        update(SIMPLE_SCORE_UPDATE_ID);
        update(MATCH_STATE_COMPLETED_UPDATE_ID);
        Intent allMatches = new Intent(ScorerActivity.this, MatchSelector.class);
        startActivity(allMatches);
    }

    private void abandonMatch() {
        update(SIMPLE_SCORE_UPDATE_ID);
        update(MATCH_STATE_ABANDONED_UPDATE_ID);
        Intent allMatches = new Intent(ScorerActivity.this, MatchSelector.class);
        startActivity(allMatches);
    }


    private void penaltyRuns(int teamAwardedPenalty) {
        if(mTeamBatting == teamAwardedPenalty){
            mCurrentRuns += PENALTY_RUNS;
            setScoreDisplay();
            update(SIMPLE_SCORE_UPDATE_ID);
        } else {
            update(PENALTY_AWARDED_TO_TEAM_NOT_BATTING_ID);
            setTargetDisplay();
        }
    }

    private void checkWin(){
        if(mTeamBatting == ScoringContract.MatchEntry.TEAM_2){
            if(mCurrentRuns >= mTarget || mCurrentWickets >= 10){
                update(SIMPLE_SCORE_UPDATE_ID);
                completeMatch();
            }
        }
    }

    @Override
    public void onMiscFragmentInteraction(int actionId, Bundle data) {
        switch(actionId){
            case MiscButtonsFragment.ABANDON_MATCH_ACTION_ID:
                abandonMatch();
                break;
            case MiscButtonsFragment.PENALTY_RUNS_ACTION_ID:
                penaltyRuns(data.getInt(MiscButtonsFragment.PENALTY_AWARDED_TO_TEAM_KEY));
                break;
            case MiscButtonsFragment.SHORT_RUNS_ACTION_ID:
                int shortRuns = data.getInt(MiscButtonsFragment.SHORT_RUNS_KEY);
                if(shortRuns < runsScoredThisBall){
                    mCurrentRuns -= shortRuns;
                    update(SIMPLE_SCORE_UPDATE_ID);
                } else {
                    Toast.makeText(this, "Impossible number of short runs", Toast.LENGTH_SHORT).show();
                }
                break;
            case MiscButtonsFragment.CUSTOM_RUNS_ID:
                int customRuns = data.getInt(MiscButtonsFragment.CUSTOM_RUNS_KEY);
                mCurrentRuns += customRuns;
                mCurrentBallsBowled++;
                runsScoredThisBall = customRuns;
                setOversDisplay();
                setScoreDisplay();
                break;
            default:
                throw new IllegalArgumentException("Invalid MiscFragmentInteraction id: " + actionId);
        }
    }
}
