package io.github.soldierinwhite.cricketscorer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import io.github.soldierinwhite.cricketscorer.data.ScoringContract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MiscButtonsFragment.MiscFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MiscButtonsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MiscButtonsFragment extends Fragment {

    private MiscFragmentInteractionListener mListener;

    public static final int ABANDON_MATCH_ACTION_ID = 1;
    public static final int PENALTY_RUNS_ACTION_ID = 2;
    public static final int SHORT_RUNS_ACTION_ID = 3;
    public static final int CUSTOM_RUNS_ID = 4;


    private static final String TEAM_1_KEY = "team1";
    private static final String TEAM_2_KEY = "team2";
    public static final String PENALTY_AWARDED_TO_TEAM_KEY = "pen";
    public static final String SHORT_RUNS_KEY = "shortRuns";
    public static final String CUSTOM_RUNS_KEY = "customRuns";

    private int mPenaltyAwardedTeam;

    private EditText numberShortEditText, customRunsEditText;

    public MiscButtonsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param team1 Team 1 for spinner.
     * @param team2 Team 2 for spinner.
     * @return A new instance of fragment MiscButtonsFragment.
     */
    public static MiscButtonsFragment newInstance(String team1, String team2) {
        MiscButtonsFragment fragment = new MiscButtonsFragment();
        Bundle b = new Bundle();
        b.putString(TEAM_1_KEY, team1);
        b.putString(TEAM_2_KEY, team2);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_misc_buttons, container, false);

        TextView abandonMatchTextView;
        ImageView submitPenaltyImageView, submitShortRunsImageView, submitCustomRunsImageView;
        Spinner penaltyRunsSpinner;

        abandonMatchTextView = (TextView) rootView.findViewById(R.id.abandon_match_text_view);
        abandonMatchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ScorerActivity", "Abandon game clicked");
                onButtonPressed(ABANDON_MATCH_ACTION_ID, null);
            }
        });

        penaltyRunsSpinner = (Spinner) rootView.findViewById(R.id.awarded_to_spinner);
        String[] teams = {getArguments().getString(TEAM_1_KEY),
                getArguments().getString(TEAM_2_KEY)};
        ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, teams);
        teamSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        penaltyRunsSpinner.setAdapter(teamSpinnerAdapter);
        penaltyRunsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (selection != null && !"".equals(selection)) {
                    if (selection.equals(getArguments().getString(TEAM_1_KEY))) {
                        mPenaltyAwardedTeam = ScoringContract.MatchEntry.TEAM_1;
                    } else {
                        mPenaltyAwardedTeam = ScoringContract.MatchEntry.TEAM_2;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mPenaltyAwardedTeam = ScoringContract.MatchEntry.TEAM_1;
            }
        });

        submitPenaltyImageView = (ImageView) rootView.findViewById(R.id.penalty_runs_submit_button);
        submitPenaltyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putInt(PENALTY_AWARDED_TO_TEAM_KEY, mPenaltyAwardedTeam);
                onButtonPressed(PENALTY_RUNS_ACTION_ID, b);
            }
        });

        numberShortEditText = (EditText) rootView.findViewById(R.id.number_short_edit_text);

        submitShortRunsImageView = (ImageView) rootView.findViewById(R.id.short_runs_submit_button);
        submitShortRunsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int shortRuns = Integer.parseInt(numberShortEditText.getText().toString().trim());
                    if (shortRuns > 0 && shortRuns < 50) {
                        Bundle b = new Bundle();
                        b.putInt(SHORT_RUNS_KEY, shortRuns);
                        onButtonPressed(SHORT_RUNS_ACTION_ID, b);
                    } else {
                        Toast.makeText(getContext(), "Enter a valid number of short runs (1-50)", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Enter a valid integer", Toast.LENGTH_SHORT).show();
                }
            }
        });

        customRunsEditText = (EditText) rootView.findViewById(R.id.custom_runs_edit_text);

        submitCustomRunsImageView = (ImageView) rootView.findViewById(R.id.custom_runs_submit_button);
        submitCustomRunsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int customRuns = Integer.parseInt(customRunsEditText.getText().toString().trim());
                    //Apparently the most amount of runs run ever
                    // (officially) while looking for a ball is 17
                    //but this app allows for even more ridiculousness
                    //should that day ever arrive
                    if (customRuns > 0 && customRuns < 50) {
                        Bundle b = new Bundle();
                        b.putInt(CUSTOM_RUNS_KEY, customRuns);
                        onButtonPressed(CUSTOM_RUNS_ID, b);
                    } else {
                        Toast.makeText(getContext(), "Enter a valid number of runs (1-50)", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Enter a valid integer", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void onButtonPressed(int id, Bundle data) {
        if (mListener != null) {
            mListener.onMiscFragmentInteraction(id, data);
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MiscFragmentInteractionListener) {
            mListener = (MiscFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MiscFragmentInteractionListener {
        void onMiscFragmentInteraction(int actionId, Bundle data);
    }
}
