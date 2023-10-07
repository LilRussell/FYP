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
import java.util.List;

public class AdminStatisticFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminGraphAdapter adapter;
    private List<AdminGraphItem> adminGraphItems;
    private DatabaseReference databaseReference;
    private static final long UPDATE_INTERVAL_MS = 30 * 1000; // 30 seconds
    private Handler handler;
    private Runnable updateGraphRunnable;
    private int previousAvailability = 0;
    private int xAxisValue = 0;
    private List<Entry> chartData = new ArrayList<>();
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

        // Initialize a Handler for periodic updates
        handler = new Handler();

        // Initialize the Runnable to update the graph
        updateGraphRunnable = new Runnable() {
            @Override
            public void run() {
                xAxisValue += 30;
                // Remove the previous listener to avoid duplicates
                databaseReference.removeEventListener(valueEventListener);

                // Start listening to database changes again
                databaseReference.addValueEventListener(valueEventListener);

                // Schedule the next update after the specified interval
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };

        // Start the initial graph update and schedule periodic updates
        handler.post(updateGraphRunnable);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the ValueEventListener and stop periodic updates when the view is destroyed
        databaseReference.removeEventListener(valueEventListener);
        handler.removeCallbacks(updateGraphRunnable);
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            adminGraphItems.clear(); // Clear the existing data

            // Find the specific location with the key "NewZ4Zx95EtmdPDui8z"
            DataSnapshot specificLocationSnapshot = dataSnapshot.child("-NewZ4Zx95EtmdPDui8z");

            if (specificLocationSnapshot.exists()) {
                String locationId = specificLocationSnapshot.getKey(); // Get the location ID
                int currentAvailability = specificLocationSnapshot.child("details").child("parkingAvailability").getValue(Integer.class);



                // Store the change in availability for the X-axis value in chartData
                chartData.add(new Entry(xAxisValue, currentAvailability));



                // Create an AdminGraphItem for the specific location with its own chart data
                AdminGraphItem item = new AdminGraphItem(locationId, "X Axis Label", "Y Axis Label", chartData);
                adminGraphItems.add(item);
            }

            adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle database error, if needed
        }
    };
}