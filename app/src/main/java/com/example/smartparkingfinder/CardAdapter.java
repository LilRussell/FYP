package com.example.smartparkingfinder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardItem> cardItemList;
    private List<String> cameraNames; // Add a field for camera names
    private Context context;
    private TestFragment testFragment;

    public CardAdapter(List<CardItem> cardItemList, List<String> cameraNames, TestFragment testFragment) {
        this.cardItemList = cardItemList;
        this.cameraNames = cameraNames; // Initialize the camera names list
        this.testFragment = testFragment;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parking_section, parent, false);

        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem cardItem = cardItemList.get(position);
        holder.bind(cardItem);

        // Check if cameraNames is not null and has data
        if (cameraNames != null && !cameraNames.isEmpty()) {
            Spinner spinnerCamera = holder.itemView.findViewById(R.id.spinner_camera);

            // Create an ArrayAdapter using the fetched camera names
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    cameraNames
            );

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Apply the adapter to the Spinner
            spinnerCamera.setAdapter(adapter);
        }

        // Get the current card ID
        String currentCardId = cardItem.getCardId();

        // Add an OnClickListener to the button inside the CardView
        Button button = holder.itemView.findViewById(R.id.btn_chg_parking1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the showRadioButtonDialog function when the button is clicked
                // Get the position of the clicked item
                int position = holder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    CardItem cardItem = cardItemList.get(position);

                    // Use cardItem data to construct the Firebase query
                    DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference()
                            .child("location")
                            .child("-Ndy9UWdeyWYe9JMziIJ")
                            .child("details")
                            .child("layout")
                            .child("Floor 0")
                            .child("card");

                    // Add a ValueEventListener to fetch the card ID
                    cardRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                                String cardId = cardSnapshot.getKey();

                                // Display the card ID in a Toast message
                                Toast.makeText(v.getContext(), "Card ID: " + cardId, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }



                if (testFragment != null) {
                    testFragment.showRadioButtonDialog(currentCardId); // Pass the card ID
                    Log.d("cardID",currentCardId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        private EditText mEditText;

        public CardViewHolder(View itemView) {
            super(itemView);
            mEditText = itemView.findViewById(R.id.editPillarName);
        }

        public void bind(CardItem cardItem) {
            mEditText.setText(cardItem.getCardText());
        }
    }
}