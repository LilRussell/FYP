package com.example.smartparkingfinder;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TestFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardItem> cardItemList;
    private List<String> cameraNames;
    private TestFragment fragment;
    private String currentCardId; // Store the current card ID
    private String parkingSlot;
    private String adminId;
    private DatabaseReference cameraRef,camerasRef;
    private ValueEventListener cameraListener;
    private Uri filePath; // To store the selected image URI
    private static final int PICK_IMAGE_REQUEST = 71; // Request code for image selection
    private StorageReference storageReference;
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
        adminId= UserData.getInstance().getUserID();
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
    public void chooseImage(String cardId) {
        currentCardId = cardId;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                // Load the selected image into the ImageView (if needed)
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), filePath);
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(filePath);
                showImagePreview(inputStream);
                uploadImageToFirebase(filePath,currentCardId);
                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {
                        cardItem.setImageUrl(filePath.toString());
                        break; // Break the loop once the selected card is found
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void showImagePreview(InputStream inputStream) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(recyclerView.getContext(),R.style.CustomAlertDialogTheme));
        builder.setTitle("Image Preview");

        // Create an ImageView to display the image
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));

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
                                builder.setTitle("Image Preview");

                                // Create an ImageView to display the image
                                ImageView imageView = new ImageView(requireContext());

                                // Load the image into the ImageView using Picasso or any other image loading library
                                Picasso.get().load(imageUrl).into(imageView);

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
                                // Handle the case where imageUrl is null (no image available for this card)
                                // You can show a message to the user or take other appropriate actions.
                            }
                        } else {
                            // Handle the case where the cardId doesn't exist in the database
                            // You can show an error message or take other appropriate actions.
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
    private void uploadImageToFirebase(Uri filePath, String cardId) {
        if (filePath != null) {
            Bundle args = getArguments(); // Retrieve fragment's arguments
            if (args != null) {
                String locationId = args.getString("locationId");
                String tabTitle = args.getString("tabTitle");

                if (locationId != null && tabTitle != null) {
                    // Get a reference to the Firebase Storage location where you want to upload the image
                    storageReference = FirebaseStorage.getInstance().getReference("card_images");
                    StorageReference imageRef = storageReference.child(cardId + ".jpg");
                    DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                            .child("location")
                            .child(locationId)
                            .child("details")
                            .child("layout")
                            .child(tabTitle)
                            .child("card")
                            .child(cardId);

                    // Upload the image to Firebase Storage
                    imageRef.putFile(filePath)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Image uploaded successfully
                                // You can get the download URL of the uploaded image
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                     cardRef.child("ImageUrl").setValue(downloadUrl);

                                });
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that occurred during the upload
                                Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                }
            }

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
    void DeleteCard(String cardId) {
        currentCardId = cardId;

        // Create a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(recyclerView.getContext(),R.style.CustomAlertDialogTheme));
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this parking section?");

        // Add buttons for Yes and No
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, so delete the card
                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {
                        String previouslySelectedCamera = cardItem.getSelectedCamera();
                        if(previouslySelectedCamera!=null&&!previouslySelectedCamera.equals("")){
                            deleteCardFromFirebase(currentCardId, previouslySelectedCamera);
                        }else{
                            deleteOnlyCardFromFirebase(currentCardId);
                        }
                    }
                }
                Iterator<CardItem> iterator = cardItemList.iterator();
                while (iterator.hasNext()) {
                    CardItem cardItem = iterator.next();
                    if (cardItem.getCardId().equals(currentCardId)) {
                        // Remove the cardItem from the list
                        iterator.remove();
                        break; // Stop the loop once the card is found and removed
                    }
                }

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No, do nothing or handle as needed
            }
        });

        // Create and show the dialog
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

    private void deleteCardFromFirebase(String cardId,String camera) {
        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");

            if (locationId != null && tabTitle != null) {
                // Remove the card from Firebase
                DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                        .child("location")
                        .child(locationId)
                        .child("details")
                        .child("layout")
                        .child(tabTitle)
                        .child("card")
                        .child(cardId);

                cardRef.removeValue();
                deleteCardFromCameraFirebase(camera);

                // Show a toast message to indicate that the card has been deleted
                Toast.makeText(requireContext(), "Parking section deleted successfully", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        }
    }
    private void deleteOnlyCardFromFirebase(String cardId) {
        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");

            if (locationId != null && tabTitle != null) {
                // Remove the card from Firebase
                DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                        .child("location")
                        .child(locationId)
                        .child("details")
                        .child("layout")
                        .child(tabTitle)
                        .child("card")
                        .child(cardId);

                cardRef.removeValue();
                // Show a toast message to indicate that the card has been deleted
                Toast.makeText(requireContext(), "Parking section deleted successfully", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        }
    }
    private void deleteCardFromCameraFirebase(String selectedCameraName) {
        if (selectedCameraName != null) {
            DatabaseReference cameraRef = FirebaseDatabase.getInstance().getReference().child("camera").child(selectedCameraName);
            // Update the assignedLocation and assignedCard fields to "None"
            cameraRef.child("assignedLocation").setValue("None");
            cameraRef.child("assignedCard").setValue("None");
            cameraRef.child("assignedTab").setValue("None");
        }
    }




    void showCameraListDialog(String cardId) {

        currentCardId = cardId;
        String cameraAdmin = adminId;
        Log.d("adminID",adminId);
        // Inflate the custom layout for the AlertDialog
        View customLayout = getLayoutInflater().inflate(R.layout.select_camera, null);

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set the custom layout as the view for the AlertDialog
        builder.setView(customLayout);

        // Find views in the custom layout
        RadioGroup cameraListRadioGroup = customLayout.findViewById(R.id.camera_list);
        Button unbindButton = customLayout.findViewById(R.id.unbind_cam_btn);
        Button okButton = customLayout.findViewById(R.id.ok_button);
        Button cancelButton = customLayout.findViewById(R.id.cancel_button);
        TextView noCamerasTextView = customLayout.findViewById(R.id.txt_no_camera);

        // Set the title and other attributes for the AlertDialog


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
                    if(cameraSnapshot.child("ownedBy").getValue(String.class).equals(cameraAdmin)&&cameraSnapshot.child("assignedLocation").getValue(String.class).equals("None")){
                        String cameraName = cameraSnapshot.child("id").getValue(String.class);

                        if (cameraName != null) {
                            Log.d("CameraListID",cameraName);
                            cameraNames.add(cameraName);
                            RadioButton radioButton = new RadioButton(requireActivity());
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
        unbindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {
                        String previouslySelectedCamera = cardItem.getSelectedCamera();
                        if (previouslySelectedCamera != null&&!previouslySelectedCamera.equals("None")&&!previouslySelectedCamera.equals("")) {
                            unbindSelectedCameraFromFirebase(previouslySelectedCamera);
                            Log.d("UnbindCamera", "Unbound camera: " + previouslySelectedCamera);
                            cardItem.setSelectedCamera("");
                            cardItem.setCardP1("Not Available");
                            cardItem.setCardP2("Not Available");
                            cardItem.setCardP3("Not Available");
                        }else{
                            Toast.makeText(v.getContext(), "No Bind Camera! Cannot Unbind!", Toast.LENGTH_LONG).show();
                        }
                        break; // Break the loop once the selected card is found
                    }
                }
                        dialog.dismiss();

                Log.d("cardID",currentCardId);
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
                    for (CardItem cardItem : cardItemList) {
                        if (cardItem.getCardId().equals(currentCardId)) {
                            String previouslySelectedCamera = cardItem.getSelectedCamera();
                            if (previouslySelectedCamera!=null&&!previouslySelectedCamera.equals("")) {
                                deleteCardFromCameraFirebase(previouslySelectedCamera);
                                updateSelectedCameraInFirebase(selectedCameraName);
                            }else {
                                updateSelectedCameraInFirebase(selectedCameraName);
                                Log.d("SelectedCamera", "Selected camera: " + selectedCameraName);
                            }
                        }

                    }

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
                DatabaseReference camRefTab = FirebaseDatabase.getInstance().getReference()
                        .child("camera")
                        .child(newTitle)
                        .child("assignedTab");

                camRefLoc.setValue(locationId);
                camRefCard.setValue(currentCardId);
                camRefTab.setValue(tabTitle);
                for (CardItem cardItem : cardItemList) {
                    if (cardItem.getCardId().equals(currentCardId)) {

                        cardItem.setSelectedCamera(newTitle);
                        break; // Break the loop once the selected card is found
                    }
                }adapter.notifyDataSetChanged();


            }
        }
    }
    private void unbindSelectedCameraFromFirebase(String selectedCameraName) {

        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");

            if (selectedCameraName != null) {
                DatabaseReference cameraRef = FirebaseDatabase.getInstance().getReference().child("camera").child(selectedCameraName);

                // Update the assignedLocation and assignedCard fields to "None"
                cameraRef.child("assignedLocation").setValue("None");
                cameraRef.child("assignedCard").setValue("None");
                cameraRef.child("assignedTab").setValue("None");
                // Update the selectedCamera field in the card
                DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                        .child("location")
                        .child(locationId)  // Make sure to replace 'locationId' with the actual value
                        .child("details")
                        .child("layout")
                        .child(tabTitle)  // Make sure to replace 'tabTitle' with the actual value
                        .child("card")
                        .child(currentCardId);
                cardRef.child("cardP1").setValue("Not Available");
                cardRef.child("cardP2").setValue("Not Available");
                cardRef.child("cardP3").setValue("Not Available");
                cardRef.child("selectedCamera").setValue(""); // Update the selectedCamera field to "None" for the current card
            }
        }
    }

    void updateCardTitle(String cardId) {
        currentCardId = cardId;

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(recyclerView.getContext(), R.style.CustomAlertDialogTheme));

        // Set up the input views
        final EditText input1 = new EditText(requireContext());
        final EditText input2 = new EditText(requireContext());
        input1.setHint("From Section");
        input2.setHint("To Section");
        input1.setTextColor(getResources().getColor(android.R.color.black));
        input2.setTextColor(getResources().getColor(android.R.color.black));

        input1.setInputType(InputType.TYPE_CLASS_TEXT);
        input2.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setTitle("Edit Section Name:"); // Set the custom title layout

        // Create a custom view for the dialog's content
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add the two EditText fields to the custom view
        layout.addView(input1);
        layout.addView(input2);

        builder.setView(layout); // Set the custom view as the content

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text1 = input1.getText().toString().trim();
                        String text2 = input2.getText().toString().trim();

                        if (!TextUtils.isEmpty(text1) && !TextUtils.isEmpty(text2)) {
                            String newTitle = text1 + " ~ " + text2;
                            // Handle the new title (e.g., save it or perform an action)
                            updateCardTitleInFirebase(newTitle);
                        } else {
                            // Handle the case where either input is empty
                            Toast.makeText(requireContext(), "Both inputs are required", Toast.LENGTH_SHORT).show();
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

    private void updateCardTitleInFirebase(String newTitle) {


        Bundle args = getArguments(); // Retrieve fragment's arguments
        if (args != null) {
            String locationId = args.getString("locationId");
            String tabTitle = args.getString("tabTitle");
            Log.d("ARGS",locationId+"tab title:"+tabTitle);

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