package com.AlForce.android.runvolution;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.AlForce.android.runvolution.history.HistoryAdapter;
import com.AlForce.android.runvolution.utils.DatabaseOpenHelper;



/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private RecyclerView historyView;
    private RecyclerView.Adapter historyViewAdapter;
    private DatabaseOpenHelper dbHelper;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseOpenHelper(getContext());

        historyView = (RecyclerView) getView().findViewById(R.id.historyView);
        historyView.setHasFixedSize(true);
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        //int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 10, getResources().getDisplayMetrics());
        historyView.setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        historyView.setLayoutManager(new LinearLayoutManager(getActivity()));

        historyViewAdapter = new HistoryAdapter(getContext(), dbHelper);
        historyView.setAdapter(historyViewAdapter);
    }
}
