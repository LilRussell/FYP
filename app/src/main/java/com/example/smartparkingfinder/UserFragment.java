package com.example.smartparkingfinder;



import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private List<UserCardItem> cardItemList;
    private UserFragment fragment;
    private DatabaseReference cameraRef;
    private ValueEventListener cameraListener;
    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment = this;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_fragment_container, container, false);
        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        cardItemList = new ArrayList<>();
        adapter = new UserCardAdapter(cardItemList, fragment); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);


        for (UserCardItem cardItem : cardItemList) {
            Log.d("CardItemID", "Card ID: " + cardItem.getCardId());
        }
        // Firebase reference to the "camera" node
        cameraRef = FirebaseDatabase.getInstance().getReference().child("camera");

// Add a ValueEventListener to monitor changes in the "camera" node
        cameraListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through the card items to update "statusP1," "statusP2," and "statusP3" based on "camera" node data
                for (UserCardItem cardItem : cardItemList) {
                    String selectedCamera = cardItem.getSelectedCamera();
                    String cardId = cardItem.getCardId();
                    // Check if the selectedCamera is not null
                    if (selectedCamera != null) {
                        DataSnapshot cameraSnapshot = dataSnapshot.child(selectedCamera);

                        if (cameraSnapshot.exists()) {
                            // Get the values of "statusA," "statusB," and "statusC" from the camera node
                            String statusA = cameraSnapshot.child("statusA").getValue(String.class);
                            String statusB = cameraSnapshot.child("statusB").getValue(String.class);
                            String statusC = cameraSnapshot.child("statusC").getValue(String.class);

                            // Check for null values before using them in the switch statement
                            if (statusA != null) {
                                switch (statusA) {
                                    case "Occupied":
                                        updateStatusInFirebase(cardId, "statusP1", "Occupied");
                                        cardItem.setStatusP1("Occupied");
                                        break;
                                    case "Empty":
                                        updateStatusInFirebase(cardId, "statusP1", "Empty");
                                        cardItem.setStatusP1("Empty");
                                        break;
                                    // Add more cases if needed
                                }
                            }

                            if (statusB != null) {
                                switch (statusB) {
                                    case "Occupied":
                                        updateStatusInFirebase(cardId, "statusP2", "Occupied");
                                        cardItem.setStatusP2("Occupied");
                                        break;
                                    case "Empty":
                                        updateStatusInFirebase(cardId, "statusP2", "Empty");
                                        cardItem.setStatusP2("Empty");
                                        break;
                                    // Add more cases if needed
                                }
                            }

                            if (statusC != null) {
                                switch (statusC) {
                                    case "Occupied":
                                        updateStatusInFirebase(cardId, "statusP3", "Occupied");
                                        cardItem.setStatusP3("Occupied");
                                        break;
                                    case "Empty":
                                        updateStatusInFirebase(cardId, "statusP3", "Empty");
                                        cardItem.setStatusP3("Empty");
                                        break;
                                    // Add more cases if needed
                                }
                            }
                        }
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        };

// Add the ValueEventListener to the cameraRef
        cameraRef.addValueEventListener(cameraListener);
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the ValueEventListener when the fragment is destroyed
        if (cameraRef != null && cameraListener != null) {
            cameraRef.removeEventListener(cameraListener);
        }
    }
    private void updateStatusInFirebase(String cardId, String statusFromCard, String statusFromCamera){
        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");

            if (locationId != null && tabTitle != null) {
                DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                        .child("location")
                        .child(locationId)
                        .child("details")
                        .child("layout")
                        .child(tabTitle)
                        .child("card")
                        .child(cardId)
                        .child(statusFromCard);
                        cardRef.setValue(statusFromCamera);
            }
        }
    }
    // Method to add a new card item to the RecyclerView
    public void addCardToRecyclerView(UserCardItem cardItem) {
        // Add the provided CardItem to the adapter's data list
        cardItemList.add(cardItem);

        // Notify the adapter of the data change
        adapter.notifyDataSetChanged();

        // Print the card IDs and the count of items in the list
        for (UserCardItem item : cardItemList) {
            Log.d("CardItemID", "Card ID: " + item.getCardId());
        }

        Log.d("CardItemCount", "Number of items in cardItemList: " + cardItemList.size());
    }
}