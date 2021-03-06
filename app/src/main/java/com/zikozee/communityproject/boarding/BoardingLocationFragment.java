package com.zikozee.communityproject.boarding;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zikozee.communityproject.ApiUtil;
import com.zikozee.communityproject.R;
import com.zikozee.communityproject.SignedInActivity;
import com.zikozee.communityproject.models.State;
import com.zikozee.communityproject.models.Vendor;
import com.zikozee.communityproject.route.Route;
import com.zikozee.communityproject.route.RouteFragment;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoardingLocationFragment extends Fragment {
    public static final String TAG = "LOCATION_FRAGMENT";

    private ProgressBar mLoadingProgress;
    private RecyclerView myRecyclerView;
    private String chosenText;

    View v;

    public BoardingLocationFragment() {
        // Required empty public constructor
    }

    public BoardingLocationFragment(String chosenText) {
        this.chosenText = chosenText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_location, container, false);

        Context context = container.getContext();
        mLoadingProgress = v.findViewById(R.id.pb_loading_location);
        mLoadingProgress.setVisibility(View.VISIBLE);

        myRecyclerView = v.findViewById(R.id.location_recyclerview);
        LinearLayoutManager learningLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        myRecyclerView.setLayoutManager(learningLayoutManager);

        try {
            URL vendorsURL = ApiUtil.buildUrl("/vendors");
            new VendorQueryTask().execute(vendorsURL);
        }catch (Exception e){
            Log.d("error", e.getMessage());
        }


//        myRecyclerView.addOnItemTouchListener(
//                new BoardingLocationRecyclerItemClickListener(context, myRecyclerView ,new BoardingLocationRecyclerItemClickListener.OnItemClickListener() {
//                    @Override public void onItemClick(View view, int position) {
//                        // do whatever
//                        Toast.makeText(context, chosenText, Toast.LENGTH_SHORT).show();
//
//                        RouteFragment routeFragment = new RouteFragment(chosenText);
//                        FragmentManager manager =  ((AppCompatActivity) context).getSupportFragmentManager();
//                        manager.beginTransaction()
//                                .replace(R.id.route_holder_fragment, routeFragment, routeFragment.getTag())
//                                .commit();
//                    }
//
//                    @Override public void onLongItemClick(View view, int position) {
//                        // do whatever
//                    }
//                })
//        );


        return v;
    }

    public class VendorQueryTask extends AsyncTask<URL, Void, String>{

        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String result = null;

            try{
                result = ApiUtil.getJson(searchURL);
            }catch (Exception e){
                Log.e("error", e.toString());
            }

            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            TextView error = v.findViewById(R.id.error_location);
            mLoadingProgress.setVisibility(View.GONE);
            if(error == null){
                myRecyclerView.setVisibility(View.INVISIBLE);
                error.setVisibility(View.VISIBLE);
            }else{
                myRecyclerView.setVisibility(View.VISIBLE);
                error.setVisibility(View.INVISIBLE);
            }
            List<Vendor> vendors = ApiUtil.getVendorFromJson(result);


            List<BoardingLocation> boardingLocations = new ArrayList<>();
            for(Vendor vendor: vendors){
                if(vendor.getName().equals(chosenText)){

                    List<State> states = ApiUtil.getStatesFromJson(vendor.getState());

                    //State Tracker
                    Set<String> statesTracker = new HashSet<>();
                    for (int i = 0; i < states.size(); i++) {
                        String stateName= states.get(i).getName();
                        if(statesTracker.contains(stateName))
                            continue;
                        statesTracker.add(stateName);
                        List<String> startLocationsList = states.stream()
                                .filter(state -> state.getName().equals(stateName))
                                .map(State::getStartLocation)
                                .collect(Collectors.toList());

                        String StartLocations = android.text.TextUtils.join(", ", startLocationsList);

                        BoardingLocation boardingLocation = new BoardingLocation.Builder()
                                .stateName(stateName)
                                .locationStart(StartLocations)
                                .build();

                        boardingLocations.add(boardingLocation);
                    }

                    break;
                }

            }

            Log.d(TAG, boardingLocations.toString());


            BoardingLocationAdapter adapter = new BoardingLocationAdapter(boardingLocations, chosenText);
            myRecyclerView.setAdapter(adapter);
        }
    }

}