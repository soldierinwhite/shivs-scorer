package io.github.soldierinwhite.cricketscorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void newMatch(View view){
        Intent newMatchIntent = new Intent(MainActivity.this, NewMatchActivity.class);
        startActivity(newMatchIntent);
    }

    public void loadMatches(View view){
        Intent loadMatchesIntent = new Intent(MainActivity.this, MatchSelector.class);
        startActivity(loadMatchesIntent);
    }
}
