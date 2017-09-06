package io.github.soldierinwhite.cricketscorer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;

/**
 * Created by schoo on 2017/02/03.
 */

public final class DisplayUtils {

    private static final int COMPLETE = ScoringContract.MatchEntry.COMPLETED;
    private static final int ABANDONED = ScoringContract.MatchEntry.ABANDONED;
    private static final int IN_PROGRESS = ScoringContract.MatchEntry.IN_PROGRESS;


    private DisplayUtils() {
    }

    public static String teamNameAbbreviate(String teamName) {
        String abbreviation = "";

        for (int i = 0; i < teamName.length(); i++) {
            if (abbreviation.length() < 3) {
                if (abbreviation.length() == 0 && Character.isLetter(teamName.charAt(i))) {
                    abbreviation = abbreviation + Character.toUpperCase(teamName.charAt(i));
                } else if (abbreviation.length() != 0 && teamName.length() - i < 3) {
                    abbreviation = abbreviation + teamName.charAt(i);
                } else if (abbreviation.length() != 0 && "aeiouäöå ".indexOf(String.valueOf(teamName.charAt(i))) == -1) {
                    abbreviation = abbreviation + teamName.charAt(i);
                }
            } else {
                i = teamName.length();
            }
        }
        return abbreviation;
    }

    public static String listableDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM ''yy");
        Date date = new Date(dateInMillis);
        return sdf.format(date);
    }

    public static String matchDescriptor(String team1, String team2) {
        return team1 + " vs " + team2;
    }

    public static String scoreDescriptor(String team1, String team2, int matchState, int scoreTeam1, int wicketsTeam1, int scoreTeam2, int wicketsTeam2, int teamBatting) {
        String score = "";
        switch (matchState) {
            case IN_PROGRESS:
                if (teamBatting == ScoringContract.MatchEntry.TEAM_1)
                    score = teamNameAbbreviate(team1) + " " + scoreTeam1 + "/" + wicketsTeam1;
                else if (wicketsTeam1 != 10 && teamBatting == ScoringContract.MatchEntry.TEAM_2)
                    score = teamNameAbbreviate(team1) + " " +
                            scoreTeam1 + "/" +
                            wicketsTeam1 + " and " +
                            teamNameAbbreviate(team2) + " " +
                            scoreTeam2 + "/" +
                            wicketsTeam2;
                else
                    score = teamNameAbbreviate(team1) + " " +
                            String.valueOf(scoreTeam1) + " and " +
                            teamNameAbbreviate(team2) + " " +
                            scoreTeam2 + "/" +
                            wicketsTeam2;
                break;
            case COMPLETE:
                if(scoreTeam1 > scoreTeam2)
                    score = teamNameAbbreviate(team1) + " won by " + (scoreTeam1 - scoreTeam2) + " runs";
                else if (scoreTeam2 > scoreTeam1)
                    score = teamNameAbbreviate(team2) + " won by " + (10 - wicketsTeam2) + " wkts";
                break;
            case ABANDONED:
                score = "Match abandoned";
                break;
            default:
                throw new IllegalArgumentException("Invalid match state found: " + matchState);
        }
        return score;
    }

    public static String overLimitDescriptor(int overLimit){
        String overLimitString;
        if(overLimit == 1){
            overLimitString = overLimit + " over";
        } else {
            overLimitString = overLimit + " overs";
        }
        return overLimitString;
    }

    public static String setTarget(int target){
        return "Target: " + target;
    }
}
