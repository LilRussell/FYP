package com.example.smartparkingfinder;



import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private List<UserCardItem> cardItemList;
    private UserFragment fragment;
    private DatabaseReference cameraRef;
    private ValueEventListener cameraListener;
    private Button findCardButton;
    private int lastScrolledPosition = -1;
    private TextView parkingCount;
    private String fragmentName;
    private String userID ;
    //="syu2kx0k6cNWc8FuUe67RIfcFxx2"
    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment = this;
        Bundle argsH = getArguments();
        if (argsH != null) {
            fragmentName = argsH.getString("locationName");
            userID=argsH.getString("userID");

        }


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_fragment_container, container, false);
        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        parkingCount = view.findViewById(R.id.txt_parking_count);
        cardItemList = new ArrayList<>();
        adapter = new UserCardAdapter(cardItemList, fragment,recyclerView); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        findCardButton= view.findViewById(R.id.btn_findCard);
        findCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstEmptySlotCard();
            }
        });
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
                updateParkingCount();
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
    public String getFragmentName() {
        return fragmentName;
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
    private void updateParkingCount() {
        int totalOccupiedCount = 0;
        int totalEmptyCount = 0;

        for (UserCardItem cardItem : cardItemList) {
            int occupiedCount = 0;
            int emptyCount = 0;

            // Check statusP1
            if ("Occupied".equals(cardItem.getStatusP1())) {
                occupiedCount++;
            } else if ("Empty".equals(cardItem.getStatusP1())) {
                emptyCount++;
            }

            // Check statusP2
            if ("Occupied".equals(cardItem.getStatusP2())) {
                occupiedCount++;
            } else if ("Empty".equals(cardItem.getStatusP2())) {
                emptyCount++;
            }

            // Check statusP3
            if ("Occupied".equals(cardItem.getStatusP3())) {
                occupiedCount++;
            } else if ("Empty".equals(cardItem.getStatusP3())) {
                emptyCount++;
            }

            totalOccupiedCount += occupiedCount;
            totalEmptyCount += emptyCount;
        }

        // Create a SpannableStringBuilder to format the text with different colors
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Add the "Parking Status: " prefix
        String prefixText = "Parking Status: ";
        builder.append(prefixText);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, prefixText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add the totalOccupiedCount in red
        String occupiedCountText = totalOccupiedCount + " Occupied ";
        builder.append(occupiedCountText);
        builder.setSpan(new ForegroundColorSpan(Color.RED), prefixText.length(), prefixText.length() + occupiedCountText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add the " / " separator
        String separatorText = " / ";
        builder.append(separatorText);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), builder.length() - separatorText.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add the totalEmptyCount in green
        String emptyCountText = totalEmptyCount + " Empty";
        builder.append(emptyCountText);
        builder.setSpan(new ForegroundColorSpan(Color.GREEN), builder.length() - emptyCountText.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the formatted text to the parkingCount TextView
        parkingCount.setText(builder);
    }
    private void scrollToFirstEmptySlotCard() {
        int startPosition = lastScrolledPosition + 1; // Start from the next position

        for (int i = startPosition; i < cardItemList.size(); i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if ("Empty".equals(cardItem.getStatusP1()) || "Empty".equals(cardItem.getStatusP2()) || "Empty".equals(cardItem.getStatusP3())) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // If no empty card is found in the remaining items, wrap around to the beginning
        for (int i = 0; i < startPosition; i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if ("Empty".equals(cardItem.getStatusP1()) || "Empty".equals(cardItem.getStatusP2()) || "Empty".equals(cardItem.getStatusP3())) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // Show a message if no card with an empty slot is found
        Toast.makeText(requireContext(), "No card with an empty slot found", Toast.LENGTH_SHORT).show();
    }
    public void btnparkedFunction(UserCardItem cardItem) {
        Bundle args = getArguments();
        if (args != null) {
            fragmentName = args.getString("locationName");
           // userID=args.getString("UserID");
           // Log.d("btnParked",userID);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Parking");
        builder.setMessage("Do you want to confirm parking at " + cardItem.getCardText() + "?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform the parking confirmation action here
                // You can update the status of the card or perform other relevant actions

                // 1. Query the "car" node to find a child with userID and value "true"
                DatabaseReference carRef = FirebaseDatabase.getInstance().getReference().child("car");
                Query query = carRef.orderByChild("userID").equalTo(userID);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Iterate through the found children (should be only one)
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                Boolean isDefault = childSnapshot.child("default").getValue(Boolean.class);
                                if (isDefault != null && isDefault) {
                                    String carNum = childSnapshot.child("numberplate").getValue(String.class);
                                    long currentTimeMillis = System.currentTimeMillis();


                                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                            .format(new Date(currentTimeMillis));

                                    // 2. Upload the information to the "history" node
                                    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
                                    String historyKey = historyRef.push().getKey(); // Generate a unique key
                                    HistoryItem historyItem = new HistoryItem(userID,carNum,cardItem.getCardText(),"test",dateTime);
                                    historyRef.child(historyKey).setValue(historyItem);
                                }


                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors here
                    }
                });

                // Update the card status and notify the adapter if needed
                // cardItem.setStatusP1("Occupied");
                // updateStatusInFirebase(cardItem.getCardId(), "statusP1", "Occupied");
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the cancel action here
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
