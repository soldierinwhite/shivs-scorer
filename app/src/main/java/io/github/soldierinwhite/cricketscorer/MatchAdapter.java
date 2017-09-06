package io.github.soldierinwhite.cricketscorer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;
import io.github.soldierinwhite.cricketscorer.util.DisplayUtils;

/**
 * Created by schoo on 2017/02/06.
 */

class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchAdapterViewHolder> {

    private final int MATCH_STATE_COMPLETE = ScoringContract.MatchEntry.COMPLETED;
    private final int MATCH_STATE_ABANDONED = ScoringContract.MatchEntry.ABANDONED;
    private final int MATCH_STATE_IN_PROGRESS = ScoringContract.MatchEntry.IN_PROGRESS;

    private final Context mContext;
    private final MatchAdapterOnClickHandler mClickHandler;

    private Cursor mCursor;

    public interface MatchAdapterOnClickHandler {
        void onClick(Bundle data);
    }

    public MatchAdapter (@NonNull Context context, MatchAdapterOnClickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public MatchAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.match_list_item, parent, false);

        view.setFocusable(true);

        return new MatchAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MatchAdapterViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position))
            return;

        int matchState = mCursor.getInt(MatchSelector.INDEX_MATCH_STATE);
        int iconId;
        switch(matchState){
            case MATCH_STATE_COMPLETE:
                iconId = R.mipmap.ic_done;
                break;
            case MATCH_STATE_ABANDONED:
                iconId = android.R.drawable.ic_delete;
                break;
            case MATCH_STATE_IN_PROGRESS:
                iconId = R.mipmap.ic_ball;
                break;
            default:
                throw new IllegalArgumentException("Invalid match state, value of: " + matchState);
        }

        holder.matchStateView.setImageResource(iconId);

        long dateInMillis = mCursor.getLong(MatchSelector.INDEX_DATE);
        String dateString = DisplayUtils.listableDate(dateInMillis);
        holder.dateTextView.setText(dateString);

        int overLimit = mCursor.getInt(MatchSelector.INDEX_OVER_LIMIT);
        String overLimitString = DisplayUtils.overLimitDescriptor(overLimit);
        holder.overLimitTextView.setText(overLimitString);

        String team1 = mCursor.getString(MatchSelector.INDEX_TEAM_1);
        String team2 = mCursor.getString(MatchSelector.INDEX_TEAM_2);
        holder.teamsTextView.setText(DisplayUtils.matchDescriptor(team1, team2));

        int scoreTeam1 = mCursor.getInt(MatchSelector.INDEX_SCORE_TEAM_1);
        int wicketsTeam1 = mCursor.getInt(MatchSelector.INDEX_WICKETS_LOST_TEAM_1);
        int scoreTeam2 = mCursor.getInt(MatchSelector.INDEX_SCORE_TEAM_2);
        int wicketsTeam2 = mCursor.getInt(MatchSelector.INDEX_WICKETS_LOST_TEAM_2);
        int teamBatting = mCursor.getInt(MatchSelector.INDEX_TEAM_BATTING);
        String score = DisplayUtils.scoreDescriptor(team1, team2, matchState, scoreTeam1, wicketsTeam1, scoreTeam2, wicketsTeam2, teamBatting);
        holder.scoreTextView.setText(score);

        long id = mCursor.getLong(MatchSelector.INDEX_ID);
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if(null == mCursor)
            return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        if(mCursor != null) mCursor.close();
        mCursor = newCursor;
        if(newCursor != null)
        this.notifyDataSetChanged();
    }

    class MatchAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView matchStateView;
        final TextView teamsTextView;
        final TextView scoreTextView;
        final TextView dateTextView;
        final TextView overLimitTextView;

        MatchAdapterViewHolder(View view){
            super(view);
            matchStateView = (ImageView) view.findViewById(R.id.match_state_image_view_list);
            teamsTextView = (TextView) view.findViewById(R.id.teams_text_view_list);
            scoreTextView = (TextView) view.findViewById(R.id.scores_text_view_list);
            dateTextView = (TextView) view.findViewById(R.id.date_text_view_list);
            overLimitTextView = (TextView) view.findViewById(R.id.over_limit_text_view_list);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            if(mCursor.getInt(MatchSelector.INDEX_MATCH_STATE) == ScoringContract.MatchEntry.IN_PROGRESS) {
                Bundle data = new Bundle();
                data.putLong(ScoringContract.MatchEntry._ID, mCursor.getLong(MatchSelector.INDEX_ID));
                data.putString(ScoringContract.MatchEntry.COLUMN_TEAM_1, mCursor.getString(MatchSelector.INDEX_TEAM_1));
                data.putString(ScoringContract.MatchEntry.COLUMN_TEAM_2, mCursor.getString(MatchSelector.INDEX_TEAM_2));
                data.putInt(ScoringContract.MatchEntry.COLUMN_OVER_LIMIT, mCursor.getInt(MatchSelector.INDEX_OVER_LIMIT));
                data.putLong(ScoringContract.MatchEntry.COLUMN_DATE, mCursor.getLong(MatchSelector.INDEX_DATE));
                data.putInt(ScoringContract.MatchEntry.COLUMN_MATCH_STATE, mCursor.getInt(MatchSelector.INDEX_MATCH_STATE));
                data.putInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_1, mCursor.getInt(MatchSelector.INDEX_SCORE_TEAM_1));
                data.putInt(ScoringContract.MatchEntry.COLUMN_SCORE_TEAM_2, mCursor.getInt(MatchSelector.INDEX_SCORE_TEAM_2));
                data.putInt(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_1, mCursor.getInt(MatchSelector.INDEX_WICKETS_LOST_TEAM_1));
                data.putInt(ScoringContract.MatchEntry.COLUMN_WICKETS_LOST_TEAM_2, mCursor.getInt(MatchSelector.INDEX_WICKETS_LOST_TEAM_2));
                data.putInt(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_1, mCursor.getInt(MatchSelector.INDEX_BALLS_FACED_TEAM_1));
                data.putInt(ScoringContract.MatchEntry.COLUMN_BALLS_FACED_TEAM_2, mCursor.getInt(MatchSelector.INDEX_BALLS_FACED_TEAM_2));
                data.putInt(ScoringContract.MatchEntry.COLUMN_TEAM_BATTING, mCursor.getInt(MatchSelector.INDEX_TEAM_BATTING));
                mClickHandler.onClick(data);
            }
        }
    }
}
