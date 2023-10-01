package com.example.smartparkingfinder;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;
public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.CardViewHolder> {

    private List<UserCardItem> cardItemList;
    private Context context;
    private UserFragment mUserFragment;
    private RecyclerView recyclerView;

    public UserCardAdapter(List<UserCardItem> cardItemList, UserFragment mUserFragment,RecyclerView recyclerView) {
        this.cardItemList = cardItemList;
        this.mUserFragment = mUserFragment;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_parking_section, parent, false);

        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        UserCardItem userCardItem = cardItemList.get(position);
        holder.bind(userCardItem);

    }
    public void scrollToPosition(int position) {
        if (position >= 0 && position < cardItemList.size()) {
            recyclerView.smoothScrollToPosition(position);
        }
    }


    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_title,txt_traffic;
        private ImageView img1,img2,img3;

        public CardViewHolder(View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.IV_Parking1);
            img2 = itemView.findViewById(R.id.IV_Parking2);
            img3 = itemView.findViewById(R.id.IV_Parking3);
            txt_title = itemView.findViewById(R.id.txt_card);
            txt_traffic = itemView.findViewById(R.id.txt_status);
        }

        public void bind(UserCardItem userCardItem) {
            txt_title.setText(userCardItem.getCardText());


            setImageResourceBasedOnCardP(img1, userCardItem.getCardP1());
            setImageResourceBasedOnCardP(img2, userCardItem.getCardP2());
            setImageResourceBasedOnCardP(img3, userCardItem.getCardP3());

            setImageResourceBasedOnStatusAndCardP(img1,userCardItem.getStatusP1(), userCardItem.getCardP1());
            setImageResourceBasedOnStatusAndCardP(img2, userCardItem.getStatusP2(), userCardItem.getCardP2());
            setImageResourceBasedOnStatusAndCardP(img3, userCardItem.getStatusP3(), userCardItem.getCardP3());

            cardTraffic(txt_traffic, userCardItem.getStatusP1(), userCardItem.getStatusP2(), userCardItem.getStatusP3());


        }
    }
    private void cardTraffic(TextView textView,String p1, String p2, String p3){
        String traffic;
        if(p1.equals("Occupied")&&p2.equals("Occupied")&&p3.equals("Occupied")){
            traffic="FULL";
            textView.setText(traffic);
        }
        else{
            traffic="";
            textView.setText(traffic);
        }
    }

    private void setImageResourceBasedOnCardP(ImageView imageView, String cardP) {
        int imageResource;

        switch (cardP) {
            case "Normal Parking":
                imageResource = R.drawable.image_grn;
                break;
            case "Disabled Parking":
                imageResource = R.drawable.image_oku_grn;
                break;
            case "Reserved Parking":
                imageResource = R.drawable.image_rsv;
                break;
            default:
                imageResource = R.drawable.image_na;
                break;
        }

        imageView.setImageResource(imageResource);
    }
    private void setImageResourceBasedOnStatusAndCardP(ImageView imageView, String status, String cardP) {
        int imageResource;

        // Check the status first
        if ("Occupied".equals(status)) {
            // Check cardP when status is "Occupied"
            switch (cardP) {
                case "Normal Parking":
                    imageResource = R.drawable.image_red; // Change to img_red for Normal Parking
                    break;
                case "Disabled Parking":
                    imageResource = R.drawable.image_oku_red; // Change to img_oku_red for Disabled Parking
                    break;
                case "Reserved Parking":
                    imageResource = R.drawable.image_rsv; // Change to img_rsv_red for Reserved Parking
                    break;
                default:
                    imageResource = R.drawable.image_na; // Change to the appropriate default image
                    break;
            }
        } else {
            // Handle the case when status is not "Occupied"
            switch (cardP) {
                case "Normal Parking":
                    imageResource = R.drawable.image_grn;
                    break;
                case "Disabled Parking":
                    imageResource = R.drawable.image_oku_grn;
                    break;
                case "Reserved Parking":
                    imageResource = R.drawable.image_rsv;
                    break;
                default:
                    imageResource = R.drawable.image_na;
                    break;
            }
        }

        imageView.setImageResource(imageResource);
    }

}