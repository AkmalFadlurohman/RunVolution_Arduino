package com.AlForce.android.runvolution;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.AlForce.android.runvolution.R;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class PetStatusFragment extends Fragment {

    String petName;
    int petLevel;
    int petXP;

    public PetStatusFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
        petName = preferences.getString("petName", "Bobby");
        petLevel = preferences.getInt("petLevel", 1);
        petXP = preferences.getInt("petXP",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet_status, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.pet_scroll_view);
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 10, getResources().getDisplayMetrics());
        scrollView.setPadding(horizontalMargin, verticalMargin, horizontalMargin, bottomMargin);
        TextView petNameView = (TextView) view.findViewById(R.id.data_pet_name);
        TextView petLevelView = (TextView) view.findViewById(R.id.data_pet_level);
        TextView petXPView = (TextView) view.findViewById(R.id.data_pet_xp);
        petNameView.setText(petName);
        petLevelView.setText(String.format("%d", petLevel));
        petXPView.setText(String.format("%d",petXP));

    }
}
