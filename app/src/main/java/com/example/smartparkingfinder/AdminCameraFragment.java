package com.example.smartparkingfinder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCameraFragment extends Fragment {
    private RecyclerView recyclerView;
    private CameraAdapter cameraAdapter;
    private List<CameraModel> cameraList;
    private String userID;
    public AdminCameraFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_camera, container, false);
        userID = UserData.getInstance().getUserID();
        recyclerView = view.findViewById(R.id.RV_Camera);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cameraList = new ArrayList<>();
        // Initialize the CameraAdapter with your cameraList
        cameraAdapter = new CameraAdapter(requireContext(),cameraList);
        recyclerView.setAdapter(cameraAdapter);
        DatabaseReference cameraRef = FirebaseDatabase.getInstance().getReference().child("camera");
        cameraRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cameraList.clear();
                List<CameraModel> newItems = new ArrayList<>();
                for (DataSnapshot cameraSnapshot : dataSnapshot.getChildren()) {

                    // You can now access properties like id, assignedLocation, assignedTab, etc.
                    String cameraName = cameraSnapshot.child("id").getValue(String.class);
                    String locationId = cameraSnapshot.child("assignedLocation").getValue(String.class);
                    String tabId = cameraSnapshot.child("assignedTab").getValue(String.class);
                    String cardId = cameraSnapshot.child("assignedCard").getValue(String.class);
                    String status = cameraSnapshot.child("status").getValue(String.class);
                    String firebaseUserID = cameraSnapshot.child("ownedBy").getValue(String.class);
                    // Check if the userID from Firebase matches the current userID
                    if (firebaseUserID!=null&&firebaseUserID .equals(userID)) {
                        // Query the database to get the names of location, tab, and card
                        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("location").child(locationId);
                        DatabaseReference tabRef = locationRef.child("details").child("layout").child(tabId);
                        DatabaseReference cardRef = tabRef.child("card").child(cardId);

                        // Get the names of location, tab, and card
                        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot locationSnapshot) {
                                String locationName = locationSnapshot.child("details").child("name").getValue(String.class);

                                tabRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot tabSnapshot) {
                                        String tabName = tabSnapshot.child("name").getValue(String.class);

                                        cardRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot cardSnapshot) {
                                                cameraList.clear();
                                                String cardName = cardSnapshot.child("cardText").getValue(String.class);

                                                // Create a CameraModel with the retrieved data
                                                CameraModel cameraModel = new CameraModel(cameraName, locationName, tabName, cardName, status, userID);
                                                newItems.add(0, cameraModel);
                                                // Add the new items to the top of the list
                                                cameraList.addAll(0, newItems);
                                                // Notify the adapter that the data has changed
                                                cameraAdapter.notifyDataSetChanged();


                                            }


                                            @Override
                                            public void onCancelled(DatabaseError cardDatabaseError) {
                                                // Handle errors here
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError tabDatabaseError) {
                                        // Handle errors here
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(DatabaseError tabDatabaseError) {
                                // Handle errors here
                            }
                        });
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });



        return view;
    }
}