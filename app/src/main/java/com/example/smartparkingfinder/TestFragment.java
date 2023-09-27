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

public class TestFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardItem> cardItemList;
    private List<String> cameraNames;
    private TestFragment fragment;
    private String currentCardId; // Store the current card ID
    private String parkingSlot;
    private String adminId;
    private DatabaseReference cameraRef;
    private ValueEventListener cameraListener;
    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment = this;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_container, container, false);
        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        cardItemList = new ArrayList<>();
        adapter = new CardAdapter(cardItemList, cameraNames, fragment); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        Bundle args = getArguments();
        if (args != null) {
            adminId = args.getString("adminIdKey");
        }

        for (CardItem cardItem : cardItemList) {
            Log.d("CardItemID", "Card ID: " + cardItem.getCardId());
        }
        // Firebase reference to the "camera" node
        cameraRef = FirebaseDatabase.getInstance().getReference().child("camera");

// Add a ValueEventListener to monitor changes in the "camera" node
        cameraListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through the card items to update "statusP1," "statusP2," and "statusP3" based on "camera" node data
                for (CardItem cardItem : cardItemList) {
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

    void showCameraListDialog(String cardId) {
        currentCardId = cardId;

        // Inflate the custom layout for the AlertDialog
        View customLayout = getLayoutInflater().inflate(R.layout.select_camera, null);

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set the custom layout as the view for the AlertDialog
        builder.setView(customLayout);

        // Find views in the custom layout
        RadioGroup cameraListRadioGroup = customLayout.findViewById(R.id.camera_list);
        Button addButton = customLayout.findViewById(R.id.add_button);
        Button okButton = customLayout.findViewById(R.id.ok_button);
        Button cancelButton = customLayout.findViewById(R.id.cancel_button);
        TextView noCamerasTextView = customLayout.findViewById(R.id.txt_no_camera);
        // Set the title and other attributes for the AlertDialog
        builder.setTitle("Add Camera");

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Create a DatabaseReference to refer to the "camera" node
        DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference().child("camera");

        // Create a list to store camera names associated with the admin
        List<String> cameraNames = new ArrayList<>();

        camerasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing radio buttons
                cameraListRadioGroup.removeAllViews();
                noCamerasTextView.setVisibility(View.GONE);
                // Clear the camera names list
                cameraNames.clear();
                // Loop through the query results
                for (DataSnapshot cameraSnapshot : dataSnapshot.getChildren()) {
                    // Assuming "name" is the field that contains the camera name
                    if(cameraSnapshot.child("ownedBy").getValue(String.class).equals(adminId)&&cameraSnapshot.child("assignedLocation").getValue(String.class).equals("None")){
                        String cameraName = cameraSnapshot.child("id").getValue(String.class);

                        if (cameraName != null) {
                            cameraNames.add(cameraName);
                            RadioButton radioButton = new RadioButton(requireContext());
                            radioButton.setText(cameraName);
                            cameraListRadioGroup.addView(radioButton);
                        }
                    }
                }
                if (cameraNames.isEmpty()) {
                    noCamerasTextView.setVisibility(View.VISIBLE);
                } else {
                    noCamerasTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });

        // Handle button clicks
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "ADD" button click
                // You can add radio buttons to the RadioGroup dynamically here
                // For example, assume camerasDataList is a list of camera names fetched from your database
                for (String cameraName : cameraNames) {
                    RadioButton radioButton = new RadioButton(requireContext());
                    radioButton.setText(cameraName); // Set the text for the radio button
                    cameraListRadioGroup.addView(radioButton);
                }
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedCameraName = null;
                // Iterate through the radio buttons to find the selected one
                for (int i = 0; i < cameraListRadioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) cameraListRadioGroup.getChildAt(i);
                    if (radioButton.isChecked()) {
                        selectedCameraName = radioButton.getText().toString();
                        break; // Exit the loop once the selected radio button is found
                    }
                }
                // Now, you have the selected camera name in the 'selectedCameraName' variable
                if (selectedCameraName != null) {
                    updateSelectedCameraInFirebase(selectedCameraName);
                    Log.d("SelectedCamera", "Selected camera: " + selectedCameraName);
                }

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "CANCEL" button click
                // You can dismiss the dialog without saving anything
                dialog.dismiss();
            }
        });
    }
    private void updateSelectedCameraInFirebase(String newTitle) {


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
                        .child(currentCardId)
                        .child("selectedCamera");
                        cardRef.setValue(newTitle);

                DatabaseReference camRefLoc = FirebaseDatabase.getInstance().getReference()
                        .child("camera")
                        .child(newTitle)
                        .child("assignedLocation");
                DatabaseReference camRefCard = FirebaseDatabase.getInstance().getReference()
                        .child("camera")
                        .child(newTitle)
                        .child("assignedCard");


                camRefLoc.setValue(locationId);
                camRefCard.setValue(currentCardId);

                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {
                        cardItem.setSelectedCamera(newTitle);
                        break; // Break the loop once the selected card is found
                    }
                }adapter.notifyDataSetChanged();


            }
        }
    }




    void updateCardTitle(String cardId) {
        currentCardId = cardId;

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set up the input view
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setTitle("Edit Card Title")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTitle = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(newTitle)) {
                            // Handle the new title (e.g., save it or perform an action)
                            updateCardTitleInFirebase(newTitle);

                        } else {
                            // Handle the case where the input is empty
                            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCardTitleInFirebase(String newTitle) {


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
                        .child(currentCardId)
                        .child("cardText");
                cardRef.setValue(newTitle);

                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {
                        cardItem.setCardText(newTitle);
                        break; // Break the loop once the selected card is found
                    }
                }adapter.notifyDataSetChanged();


            }
        }
    }


    void showRadioButtonDialog(String cardId,String Slot) {
        // Store the current card ID
        currentCardId = cardId;
        parkingSlot = Slot;
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom dialog layout
        View customView = getLayoutInflater().inflate(R.layout.img_radio_grp_layout, null);
        RadioGroup radioGroup = customView.findViewById(R.id.radioGroup_ImgParking);

        // Set the selected option (initially, select the first option)
        final int[] selectedOption = {0};

        builder.setView(customView)
                .setTitle("Select an option")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the selected option here (e.g., save it or perform an action)
                        int checkedId = radioGroup.getCheckedRadioButtonId();
                        RadioButton radioButton = customView.findViewById(checkedId);

                        if (radioButton != null) {
                            String selectedOptionText = radioButton.getText().toString();
                            Toast.makeText(requireContext(), "Selected option: " + selectedOptionText, Toast.LENGTH_SHORT).show();

                            // Save the selected option to Firebase using the currentCardId
                            saveSelectedOptionToFirebase(currentCardId, selectedOptionText,parkingSlot);
                            Log.d("cardID",currentCardId);
                            // Update the image immediately based on the selected option
                            updateImageBasedOnOption(selectedOptionText,parkingSlot);

                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle Cancel button click or dismiss the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void updateImageBasedOnOption(String selectedOptionText, String parkingSlot) {
        // Find the ImageView within the current CardView and update its image
        for (CardItem cardItem : cardItemList) {
            if (cardItem.getCardId().equals(currentCardId)) {
                if(parkingSlot.equals("cardP1")){
                    cardItem.setCardP1(selectedOptionText);
                }
                if(parkingSlot.equals("cardP2")){
                    cardItem.setCardP2(selectedOptionText);
                }
                if(parkingSlot.equals("cardP3")){
                    cardItem.setCardP3(selectedOptionText);
                }

            } adapter.notifyDataSetChanged();
        }
    }

    private void saveSelectedOptionToFirebase(String cardId, String selectedOptionText,String Slot) {
        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");

            if (locationId != null && tabTitle != null) {
                // Construct the database path
                DatabaseReference selectedOptionRef = FirebaseDatabase.getInstance().getReference()
                        .child("location")
                        .child(locationId)
                        .child("details")
                        .child("layout")
                        .child(tabTitle)
                        .child("card")
                        .child(cardId)
                        .child(Slot);

                // Set the selected option in Firebase
                selectedOptionRef.setValue(selectedOptionText);
            }
        }
    }

    // Method to add a new card item to the RecyclerView
    public void addCardToRecyclerView(CardItem cardItem) {
        // Add the provided CardItem to the adapter's data list
        cardItemList.add(cardItem);

        // Notify the adapter of the data change
        adapter.notifyDataSetChanged();

        // Print the card IDs and the count of items in the list
        for (CardItem item : cardItemList) {
            Log.d("CardItemID", "Card ID: " + item.getCardId());
        }

        Log.d("CardItemCount", "Number of items in cardItemList: " + cardItemList.size());
    }
}