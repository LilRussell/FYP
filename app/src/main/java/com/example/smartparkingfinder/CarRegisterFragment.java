package com.example.smartparkingfinder;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CarRegisterFragment extends Fragment {

    private Button addCarButton;
    private RecyclerView recyclerView;
    private TextView txt_carNotice;
    private ArrayList<CarModel> carList;
    private CarRegisterAdapter carAdapter;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    private String userID;

    public CarRegisterFragment() {
        // Required empty public constructor
    }

    public static CarRegisterFragment newInstance(String param1, String param2) {
        CarRegisterFragment fragment = new CarRegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carList = new ArrayList<>();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle args = getArguments();
        if (args != null) {
            userID = args.getString("userID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_register, container, false);
        recyclerView = view.findViewById(R.id.RV_car);
        txt_carNotice = view.findViewById(R.id.txt_carRegisterNotice);
        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference carRef = database.getReference("car");
        // Initialize your ArrayList for car data (carList)
        carList = new ArrayList<>();

        // Initialize your CarAdapter with carList and the current context
        carAdapter = new CarRegisterAdapter(carList,this );

        // Set the layout manager for the RecyclerView (e.g., LinearLayoutManager)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Attach the adapter to the RecyclerView
        recyclerView.setAdapter(carAdapter);

        addCarButton = view.findViewById(R.id.add_car);
        // Initialize your RecyclerView, adapter, and other views here.

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCarDialog();
            }
        });
        carRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                carList.clear(); // Clear the existing list

                for (DataSnapshot carSnapshot : dataSnapshot.getChildren()) {
                    // Deserialize the car data from the snapshot
                    CarModel car = carSnapshot.getValue(CarModel.class);

                    if (car != null && car.getUserID().equals(userID)) {
                        // Only add the car to your carList if its userID matches the current user's userID
                        carList.add(car);
                    }
                }

                // Notify the RecyclerView adapter that data has changed
                carAdapter.notifyDataSetChanged();
                // Check if the carList is empty and show the notice if it is
                if (carList.isEmpty()) {
                    txt_carNotice.setVisibility(View.VISIBLE);
                } else {
                    txt_carNotice.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur when reading from the database
            }
        });
        return view;
    }
    void updateDefaultCar(int newPosition) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference carRef = database.getReference("car");
        if (newPosition >= 0 && newPosition < carList.size()) {
            // Get the currently selected car
            CarModel selectedCar = carList.get(newPosition);

            // Find the previous default car and update it to false
            for (int i = 0; i < carList.size(); i++) {
                CarModel car = carList.get(i);
                if (car.isDefault() && i != newPosition) {
                    car.setDefault(false);
                    // Update the previous default car's data in Firebase
                    DatabaseReference previousDefaultCarRef = carRef.child(car.getCarID()); // Adjust this based on your data structure
                    previousDefaultCarRef.setValue(car);
                }
            }

            // Mark the selected car as default
            selectedCar.setDefault(true);

            // Update the RecyclerView
            carAdapter.notifyDataSetChanged();

            // Update the selected car's data in Firebase
            DatabaseReference selectedCarRef = carRef.child(selectedCar.getCarID()); // Adjust this based on your data structure
            selectedCarRef.setValue(selectedCar);
        }
    }
    void deleteCarFromFirebase(String carIDToDelete) {
        // Create a DatabaseReference that points to the specific car to delete
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference carRef = database.getReference("car");
        DatabaseReference carToDeleteRef = carRef.child(carIDToDelete); // Adjust this based on your data structure

        // Delete the car from Firebase
        carToDeleteRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Car deletion was successful
                Toast.makeText(getActivity(), "Car Deleted Successfully", Toast.LENGTH_LONG).show();
                // You can update your local data (carList) if needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to delete the car
            }
        });
    }

    private void showAddCarDialog() {
        // Create a ContextThemeWrapper with your custom style
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogTheme);

        // Create an AlertDialog.Builder with the themed context
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setTitle("Register Your Car");
        // Create a parent layout (e.g., LinearLayout) to hold the EditText fields
        LinearLayout parentLayout = new LinearLayout(contextThemeWrapper);
        parentLayout.setOrientation(LinearLayout.VERTICAL);

        // Add EditText fields for numberplate and model to the parent layout
        final EditText numberplateEditText = new EditText(contextThemeWrapper);
        numberplateEditText.setHint("Numberplate");
        numberplateEditText.setTextColor(getResources().getColor(R.color.black));
        parentLayout.addView(numberplateEditText); // Add to parent layout

        final EditText modelEditText = new EditText(contextThemeWrapper);
        modelEditText.setHint("Model");
        modelEditText.setTextColor(getResources().getColor(R.color.black));
        parentLayout.addView(modelEditText); // Add to parent layout

        // Set the parent layout as the dialog's view
        builder.setView(parentLayout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the values from EditText fields
                String numberplate = numberplateEditText.getText().toString();
                String model = modelEditText.getText().toString();
                DatabaseReference carRef = FirebaseDatabase.getInstance().getReference("car");

                // Check if a car with the same number plate already exists and is owned by the current user
                carRef.orderByChild("numberplate").equalTo(numberplate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean carExists = false;

                        for (DataSnapshot carSnapshot : dataSnapshot.getChildren()) {
                            CarModel existingCar = carSnapshot.getValue(CarModel.class);

                            if (existingCar != null && existingCar.getUserID().equals(userID)) {
                                // A car with the same number plate and owned by the current user already exists
                                carExists = true;
                                break; // Exit the loop
                            }
                        }

                        if (carExists) {
                            // A car with the same number plate and owned by the current user already exists
                            Toast.makeText(getActivity(), "A car with the same number plate already exists. Please use a different number plate.", Toast.LENGTH_LONG).show();
                        } else {
                            // Number plate is unique, proceed with adding the new car
                            String carId = carRef.push().getKey();
                            CarModel newCar = new CarModel(carId, userID, numberplate, model, false);
                            carRef.child(carId).setValue(newCar);

                            // Add the newCar to your carList (RecyclerView data source)
                            carList.add(newCar);

                            // Notify the RecyclerView adapter that data has changed
                            carAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors that occur when reading from the database
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
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