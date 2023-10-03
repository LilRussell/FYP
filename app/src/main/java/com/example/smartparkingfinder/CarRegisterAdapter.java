package com.example.smartparkingfinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CarRegisterAdapter extends RecyclerView.Adapter<CarRegisterAdapter.ViewHolder> {

    private List<CarModel> carList;

    private CarRegisterFragment carRegisterFragment;
    public CarRegisterAdapter(List<CarModel> carList, CarRegisterFragment carRegisterFragment) {
        this.carList = carList;
        this.carRegisterFragment=carRegisterFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_register_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CarModel car = carList.get(position);

        // Bind car data to the ViewHolder's views
        holder.numberplateTextView.setText(car.getNumberplate());
        holder.modelTextView.setText(car.getModel());
        // Check if the car is the default one
        // Check if the car is the default one
        if (car.isDefault()) {
            holder.starImageView.setVisibility(View.VISIBLE); // Show the star image
            holder.setDefaultButton.setVisibility(View.INVISIBLE); // Hide the "Set Default" button
        } else {
            holder.starImageView.setVisibility(View.INVISIBLE); // Hide the star image
            holder.setDefaultButton.setVisibility(View.VISIBLE); // Show the "Set Default" button
        }


        // Set an OnClickListener for the "Set Default" button
        holder.setDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carRegisterFragment.updateDefaultCar(position);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the position of the clicked item
                int clickedPosition = holder.getAdapterPosition();

                // Get the car ID of the clicked car
                String carIDToDelete = carList.get(clickedPosition).getCarID(); // Adjust this based on your data structure

                // Remove the clicked car from the carList
                carList.remove(clickedPosition);

                // Notify the adapter that data has changed to update the UI
                notifyItemRemoved(clickedPosition);

                // Call the deleteCarFromFirebase function in the fragment to delete the car from Firebase
                carRegisterFragment.deleteCarFromFirebase(carIDToDelete);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberplateTextView;
        TextView modelTextView;
        ImageView starImageView,deleteButton;
        Button setDefaultButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            starImageView = itemView.findViewById(R.id.IV_star);
            numberplateTextView = itemView.findViewById(R.id.txt_numberplate);
            modelTextView = itemView.findViewById(R.id.txt_car_model);
            setDefaultButton = itemView.findViewById(R.id.btn_Car_set_default);
            deleteButton = itemView.findViewById(R.id.IV_Car_Delete);
        }
    }
}
