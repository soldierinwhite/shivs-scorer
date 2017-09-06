package io.github.soldierinwhite.cricketscorer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.content.AsyncTaskLoader;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;

/**
 * Created by schoo on 2017/02/02.
 */

class MatchLoader extends AsyncTaskLoader<Cursor>{
    private Uri mUri;

    public MatchLoader(Context context, Uri uri){
        super(context);
        mUri = uri;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        if (mUri == null) {
            return null;
        }
        return getContext().getContentResolver().query(mUri, null, null, null, ScoringContract.MatchEntry.COLUMN_DATE);
    }
}
