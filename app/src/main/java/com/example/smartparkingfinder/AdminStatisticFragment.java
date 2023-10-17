package com.example.smartparkingfinder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminStatisticFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminGraphAdapter adapter;
    private List<AdminGraphItem> adminGraphItems;
    private DatabaseReference databaseReference;
    private static final long UPDATE_INTERVAL_MS = 15 * 1000; // 15 seconds
    private long lastUpdateTime = 0;
    private Handler handler;
    private Runnable updateGraphRunnable;
    private int previousAvailability = 0;
    private int xAxisValue = 0;
    private List<Entry> chartData = new ArrayList<>();
    private Map<String, Entry> previousDataPoints = new HashMap<>();
    private Map<String, List<Entry>> historicalDataPoints = new HashMap<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistic, container, false);

        recyclerView = view.findViewById(R.id.RV_Graph);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adminGraphItems = new ArrayList<>();
        adapter = new AdminGraphAdapter(getActivity(), adminGraphItems);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Database reference for the location path
        databaseReference = FirebaseDatabase.getInstance().getReference().child("location");
        startGraphUpdate();

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        startGraphUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        startGraphUpdate();
    }

    private void startGraphUpdate() {
        handler = new Handler();
        updateGraphRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                    fetchAndPlotDataForAllLocations();
                    lastUpdateTime = currentTime;
                }
                handler.post(this); // Continue the periodic update
            }
        };
        handler.post(updateGraphRunnable);
    }

    private void stopGraphUpdate() {
        if (handler != null && updateGraphRunnable != null) {
            handler.removeCallbacks(updateGraphRunnable);
        }
    }

    private void fetchAndPlotDataForAllLocations() {
        // Fetch data for all locations
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adminGraphItems.clear(); // Clear previous graph items

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String locationName = locationSnapshot.child("details").child("name").getValue(String.class);
                    Integer currentAvailability = locationSnapshot.child("details").child("parkingAvailability").getValue(Integer.class);

                    if (locationName != null && currentAvailability != null) {
                        // Create or update a graph item for each location
                        AdminGraphItem graphItem = getGraphItemForLocation(locationName);
                        List<Entry> chartData = graphItem.getChartData();

                        // Retrieve the historical data points for this location
                        List<Entry> historicalData = historicalDataPoints.getOrDefault(locationName, new ArrayList<>());

                        // Add the new data point to the historical data
                        historicalData.add(new Entry(xAxisValue, currentAvailability));

                        // Add all historical data points to the chart data
                        chartData.addAll(historicalData);

                        // Notify the adapter that the data has changed
                        adapter.notifyDataSetChanged();

                        // Update the historical data for this location
                        historicalDataPoints.put(locationName, historicalData);
                        xAxisValue += 1; // Increment x by 1 for each data point
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
    private AdminGraphItem getGraphItemForLocation(String locationName) {
        for (AdminGraphItem graphItem : adminGraphItems) {
            if (graphItem.getTitle().equals(locationName)) {
                return graphItem; // Return the existing graph item for the location
            }
        }

        // If no graph item exists for the location, create a new one
        AdminGraphItem newGraphItem = new AdminGraphItem(locationName, "X Axis Label", "Y Axis Label", new ArrayList<>());
        adminGraphItems.add(newGraphItem);
        return newGraphItem;
    }
}

