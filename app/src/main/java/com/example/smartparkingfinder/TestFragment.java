package com.example.smartparkingfinder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TestFragment extends Fragment {
    private static final String TAG = "TestFragment";
    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardItem> cardItemList;
    private List<String> cameraNames;
    private TestFragment fragment;
    private String currentCardId; // Store the current card ID

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment = this;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_container, container, false);
        Log.d(TAG, "onCreateView called for Fragment " + getArguments().getString("tabTitle"));

        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        cardItemList = new ArrayList<>();
        adapter = new CardAdapter(cardItemList, cameraNames, fragment); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Fetch camera names from Firebase
        DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference("camera").child("P8keeLX1JLSM8NBa6PiE2O8vtWW2");
        camerasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> cameraNames = new ArrayList<>();
                cameraNames.add("Unassigned");
                for (DataSnapshot cameraSnapshot : dataSnapshot.getChildren()) {
                    String cameraName = cameraSnapshot.child("name").getValue(String.class);
                    if (cameraName != null) {
                        cameraNames.add(cameraName);
                    }
                }
                // Pass the camera names to the adapter when creating an instance of it
                adapter = new CardAdapter(cardItemList, cameraNames, fragment);

                // Set up the RecyclerView
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);

                // Add camera names to the RecyclerView
                for (String cameraName : cameraNames) {
                    addCardToRecyclerView(cameraName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        return view;
    }

    void showRadioButtonDialog(String cardId) {
        // Store the current card ID
        currentCardId = cardId;

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
                            saveSelectedOptionToFirebase(currentCardId, selectedOptionText);
                            Log.d("cardID",currentCardId);                        }
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

    private void saveSelectedOptionToFirebase(String cardId, String selectedOptionText) {
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
                        .child("img1");

                // Set the selected option in Firebase
                selectedOptionRef.setValue(selectedOptionText);
            }
        }
    }

    // Method to add a new card item to the RecyclerView
    public void addCardToRecyclerView(String cardText) {
        // Create a new CardItem and add it to the adapter's data list
        CardItem cardItem = new CardItem("",cardText);
        cardItemList.add(cardItem);

        // Notify the adapter of the data change
        adapter.notifyDataSetChanged();
    }
}