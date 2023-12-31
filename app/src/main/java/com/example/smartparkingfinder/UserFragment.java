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
import android.view.ContextThemeWrapper;
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
import com.squareup.picasso.Picasso;


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
    private Button findCardButton,findNormalButton,findDisabledButton;
    private int lastScrolledPosition = -1;
    private TextView parkingCount,txtNothing;
    private String fragmentName;
    private String userID ;
    private String tabtitle;
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
            tabtitle=argsH.getString("fragmentName");
        }


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_fragment_container, container, false);
        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        parkingCount = view.findViewById(R.id.txt_parking_count);
        cardItemList = new ArrayList<>();
        txtNothing=view.findViewById(R.id.txt_nothing_user_section);
        adapter = new UserCardAdapter(cardItemList, fragment,recyclerView); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        findCardButton= view.findViewById(R.id.btn_findCard);
        findNormalButton= view.findViewById(R.id.btn_findNormalCard);
        findDisabledButton= view.findViewById(R.id.btn_findDisabledCard);
        updateParkingCount();
        findCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstEmptySlotCard();
            }
        });
        findNormalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstEmptyNormalSlotCard();
            }
        });
        findDisabledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstEmptyDisabledSlotCard();
            }
        });
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
                updateParkingCount();
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
        updateParkingCount();
        // Notify the adapter of the data change
        adapter.notifyDataSetChanged();
        if(adapter.getItemCount()>0||!cardItemList.isEmpty()){
            txtNothing.setVisibility(View.GONE);
        }else{
            txtNothing.setVisibility(View.VISIBLE);
        }
        // Print the card IDs and the count of items in the list
        for (UserCardItem item : cardItemList) {
            Log.d("CardItemID", "Card ID: " + item.getCardId());
        }

        Log.d("CardItemCount", "Number of items in cardItemList: " + cardItemList.size());
    }
    public void updateParkingCount() {
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
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // Show a message if no card with an empty slot is found
        Toast.makeText(requireContext(), "No Empty Parking found", Toast.LENGTH_SHORT).show();
    }
    private void scrollToFirstEmptyNormalSlotCard() {
        int startPosition = lastScrolledPosition + 1; // Start from the next position

        for (int i = startPosition; i < cardItemList.size(); i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if (("Empty".equals(cardItem.getStatusP1()) && "Normal Parking".equals(cardItem.getCardP1())) || ("Empty".equals(cardItem.getStatusP2()) && "Normal Parking".equals(cardItem.getCardP2()))||("Empty".equals(cardItem.getStatusP3()) && "Normal Parking".equals(cardItem.getCardP3()))) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // If no empty card is found in the remaining items, wrap around to the beginning
        for (int i = 0; i < startPosition; i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if (("Empty".equals(cardItem.getStatusP1()) && "Normal Parking".equals(cardItem.getCardP1())) || ("Empty".equals(cardItem.getStatusP2()) && "Normal Parking".equals(cardItem.getCardP2()))||("Empty".equals(cardItem.getStatusP3()) && "Normal Parking".equals(cardItem.getCardP3()))) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // Show a message if no card with an empty slot is found
        Toast.makeText(requireContext(), "No Empty Parking found", Toast.LENGTH_SHORT).show();
    }
    private void scrollToFirstEmptyDisabledSlotCard() {
        int startPosition = lastScrolledPosition + 1; // Start from the next position

        for (int i = startPosition; i < cardItemList.size(); i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if (("Empty".equals(cardItem.getStatusP1()) && "Disabled Parking".equals(cardItem.getCardP1())) || ("Empty".equals(cardItem.getStatusP2()) && "Disabled Parking".equals(cardItem.getCardP2()))||("Empty".equals(cardItem.getStatusP3()) && "Disabled Parking".equals(cardItem.getCardP3()))) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // If no empty card is found in the remaining items, wrap around to the beginning
        for (int i = 0; i < startPosition; i++) {
            UserCardItem cardItem = cardItemList.get(i);
            if (("Empty".equals(cardItem.getStatusP1()) && "Disabled Parking".equals(cardItem.getCardP1())) || ("Empty".equals(cardItem.getStatusP2()) && "Disabled Parking".equals(cardItem.getCardP2()))||("Empty".equals(cardItem.getStatusP3()) && "Disabled Parking".equals(cardItem.getCardP3()))) {
                // Scroll to the card with an empty slot
                adapter.scrollToPosition(i);
                cardItem.setHighlighted(true);
                // Notify the adapter to update the view
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Parking Found at: "+cardItem.getCardText(), Toast.LENGTH_SHORT).show();
                lastScrolledPosition = i; // Update the last scrolled position
                return; // Stop searching after the first card with an empty slot is found
            }
        }

        // Show a message if no card with an empty slot is found
        Toast.makeText(requireContext(), "No Empty Disabled Parking found", Toast.LENGTH_SHORT).show();
    }
    public void showImageFromFirebase(String cardId) {
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
                        .child(cardId);

                cardRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String imageUrl = dataSnapshot.child("ImageUrl").getValue(String.class);

                            if (imageUrl != null) {
                                // Create an alert dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(recyclerView.getContext(),R.style.CustomAlertDialogTheme));
                                builder.setTitle("Where is the location?");

                                // Create an ImageView to display the image
                                ImageView imageView = new ImageView(requireContext());


                                // Load the image into the ImageView using Picasso or any other image loading library
                                Picasso.get().load(imageUrl).into(imageView);
                                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                ));

                                // Add the ImageView to the dialog's layout
                                builder.setView(imageView);

                                // Add a "Close" button to dismiss the dialog
                                builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

                                // Create and show the AlertDialog
                                AlertDialog dialog = builder.create();
                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {

                                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                        // Set text color for negative button
                                        negativeButton.setTextColor(getResources().getColor(R.color.black));
                                    }
                                });
                                dialog.show();
                            } else {
                                Toast.makeText(requireContext(), "No Information Added.", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors here
                    }
                });
            }
        }
    }
    public void btnparkedFunction(UserCardItem cardItem) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireContext(),R.style.CustomAlertDialogTheme));
        builder.setTitle("Confirm Parking");
        builder.setMessage("Do you want to confirm parking at " + cardItem.getCardText() + "?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                                    String title= tabtitle;
                                    // 2. Upload the information to the "history" node
                                    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
                                    String historyKey = historyRef.push().getKey(); // Generate a unique key
                                    HistoryItem historyItem = new HistoryItem(userID,carNum,cardItem.getCardText(),title,fragmentName,dateTime);
                                    historyRef.child(historyKey).setValue(historyItem);

                                }


                            }
                            Toast.makeText(requireContext(), "Saved Details To History.", Toast.LENGTH_LONG).show();

                        }
                        else {
                            // No car found, show a toast message to the user
                            Toast.makeText(requireContext(), "Failed To Save History. Please register your car.\n Make Sure to Set Default Car.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors here
                    }
                });

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
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Set text color for positive button
                positiveButton.setTextColor(getResources().getColor(R.color.black));

                // Set text color for negative button
                negativeButton.setTextColor(getResources().getColor(R.color.black));
            }
        });
        dialog.show();
    }
}
