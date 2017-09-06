package io.github.soldierinwhite.cricketscorer;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;

/**
 * Created by schoo on 2017/02/06.
 */

public class TossFragment extends Fragment{

    private Spinner mBattingFirstSpinner;
    private TextView mCoin;
    private Button mPlayButton;

    private int mBattingFirst = ScoringContract.MatchEntry.TEAM_1;
    private boolean mIsSpinnerSelected = false;

    private static final String TEAM_1_KEY = "team1";
    private static final String TEAM_2_KEY = "team2";
    private static final String OVER_LIMIT_KEY = "overLimit";


    public TossFragment(){

    }

    static TossFragment newInstance(String team1, String team2, int overLimit){
        TossFragment tossFragment = new TossFragment();
        Bundle b = new Bundle();
        b.putString(TEAM_1_KEY, team1);
        b.putString(TEAM_2_KEY, team2);
        b.putInt(OVER_LIMIT_KEY, overLimit);
        tossFragment.setArguments(b);
        return tossFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_toss, container, false);

        mBattingFirstSpinner = (Spinner) rootView.findViewById(R.id.batting_first_spinner);
        mCoin = (TextView) rootView.findViewById(R.id.coin);
        mPlayButton = (Button) rootView.findViewById(R.id.start_game_button);

        String[] teams = {getArguments().getString(TEAM_1_KEY), getArguments().getString(TEAM_2_KEY)};
        ArrayAdapter teamSpinnerAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, teams);
        teamSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mBattingFirstSpinner.setAdapter(teamSpinnerAdapter);
        mBattingFirstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if(selection != null && !"".equals(selection)){
                    mIsSpinnerSelected = true;
                    if(selection.equals(getArguments().getString(TEAM_1_KEY))){
                        mBattingFirst = ScoringContract.MatchEntry.TEAM_1;
                    } else {
                        mBattingFirst = ScoringContract.MatchEntry.TEAM_2;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tossCoin();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScorer();
            }
        });

        return rootView;
    }

    public void tossCoin(){
        Random rand = new Random();
        int headsOrTails = rand.nextInt(2);
        if(headsOrTails == 0){
            mCoin.setText(getString(R.string.heads));
        } else {
            mCoin.setText(getString(R.string.tails));
        }
    }

    public void startScorer(){
        if(!mIsSpinnerSelected){
            Toast.makeText(getActivity(), "Select the team batting first", Toast.LENGTH_SHORT).show();
            return;
        }

        String team1String = getArguments().getString(TEAM_1_KEY);
        String team2String = getArguments().getString(TEAM_2_KEY);
        int overLimit = getArguments().getInt(OVER_LIMIT_KEY);

        String team;
        if(mBattingFirst == ScoringContract.MatchEntry.TEAM_2){
            team = team1String;
            team1String = team2String;
            team2String = team;
        }

        ContentValues values = new ContentValues();
        values.put(ScoringContract.MatchEntry.COLUMN_TEAM_1, team1String);
        values.put(ScoringContract.MatchEntry.COLUMN_TEAM_2, team2String);
        if(overLimit != ScoringContract.MatchEntry.UNLIMITED_OVERS) {
            values.put(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT, overLimit);
        }
        values.put(ScoringContract.MatchEntry.COLUMN_MATCH_STATE, ScoringContract.MatchEntry.IN_PROGRESS);
        values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2, 0);
        values.put(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING, ScoringContract.MatchEntry.TEAM_1);

        Uri result = getActivity().getContentResolver().insert(ScoringContract.MatchEntry.CONTENT_URI, values);
        if (result == null) {
            Toast.makeText(getActivity(), "Error creating match. Try again.", Toast.LENGTH_SHORT);
        } else {
            Intent scoring = new Intent(getActivity(), ScorerActivity.class);
            Bundle data = new Bundle();
            data.putString(ScoringContract.MatchEntry.COLUMN_TEAM_1, team1String);
            data.putString(ScoringContract.MatchEntry.COLUMN_TEAM_2, team2String);
            data.putInt(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT, overLimit);
            scoring.putExtra(getString(R.string.match_uri_extra), result.toString());
            scoring.putExtra(getString(R.string.new_match_boolean_extra), true);
            scoring.putExtra(getString(R.string.match_data_bundle), data);
            startActivity(scoring);
        }

    }
}
