package io.github.soldierinwhite.cricketscorer;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;

/**
 * Created by schoo on 2017/02/02.
 */

public class NewMatchActivity extends AppCompatActivity {
    private EditText mTeam1EditText;
    private EditText mTeam2EditText;
    private EditText mOverLimitEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_match);

        mTeam1EditText = (EditText) findViewById(R.id.team_one_edit_text);
        mTeam2EditText = (EditText) findViewById(R.id.team_two_edit_text);
        mOverLimitEditText = (EditText) findViewById(R.id.over_limit_edit_text);
    }

    public void toss(View view) {
        String team1string = mTeam1EditText.getText().toString().trim();
        if (team1string.isEmpty() || team1string == null) {
            Toast.makeText(this, "Enter both team names", Toast.LENGTH_SHORT).show();
            return;
        }

        String team2string = mTeam2EditText.getText().toString().trim();
        if (team2string.isEmpty() || team2string == null) {
            Toast.makeText(this, "Enter both team names", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer overLimit = Integer.parseInt(mOverLimitEditText.getText().toString().trim());
        if (overLimit != null && (overLimit > 100 || overLimit <= 0)) {
            Toast.makeText(this, "Choose an over limit of 1 - 100, or leave blank for unlimited overs", Toast.LENGTH_SHORT).show();
        }

        //for unlimited overs
        if (overLimit == null)
            overLimit = ScoringContract.MatchEntry.UNLIMITED_OVERS;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.frame_layout, TossFragment.newInstance(team1string, team2string, overLimit));
        ft.commit();
    }
}
