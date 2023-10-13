package com.example.smartparkingfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {
    private List<CameraModel> cameraList;
    private Context context;

    public CameraAdapter(Context context, List<CameraModel> cameraList) {
        this.context = context;
        this.cameraList = cameraList;
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.camera_item, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        CameraModel camera = cameraList.get(position);
        String location = camera.getLocationName()+"/"+camera.getFloorName()+"/"+camera.getCardName();
        // Set camera information to the views
        holder.txtCameraName.setText(camera.getCameraName());
        holder.txtCameraLocation.setText(location);
        holder.txtCameraStatus.setText(camera.getStatus());
    }

    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    public class CameraViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCameraName, txtCameraLocation, txtCameraStatus;

        public CameraViewHolder(View itemView) {
            super(itemView);
            txtCameraName = itemView.findViewById(R.id.txt_camera_name);
            txtCameraLocation = itemView.findViewById(R.id.txt_camera_location);
            txtCameraStatus = itemView.findViewById(R.id.txt_camera_status);
        }
    }
}
