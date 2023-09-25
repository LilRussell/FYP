package com.example.smartparkingfinder;
import android.content.Context;
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


        // Get the current card ID
        String currentCardId = cardItem.getCardId();

        // Add an OnClickListener to the button inside the CardView
        Button button1 = holder.itemView.findViewById(R.id.btn_chg_parking1);
        Button button2 = holder.itemView.findViewById(R.id.btn_chg_parking2);
        Button button3 = holder.itemView.findViewById(R.id.btn_chg_parking3);
        ImageView editTitleImageView = holder.itemView.findViewById(R.id.edit_IV_Title); // Replace with your ImageView ID
        ImageView editCameraImageView = holder.itemView.findViewById(R.id.edit_IV_Camera);
        editTitleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFragment.updateCardTitle(currentCardId);
            }
        });
        editCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testFragment.showCameraListDialog(currentCardId);

            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testFragment != null) {
                    String slot = "cardP1";
                    testFragment.showRadioButtonDialog(currentCardId,slot); // Pass the card ID
                    Log.d("cardID", currentCardId);
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testFragment != null) {
                    String slot = "cardP2";
                    testFragment.showRadioButtonDialog(currentCardId,slot); // Pass the card ID
                    Log.d("cardID", currentCardId);
                }
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testFragment != null) {
                    String slot = "cardP3";
                    testFragment.showRadioButtonDialog(currentCardId,slot); // Pass the card ID
                    Log.d("cardID", currentCardId);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_title,txt_camera;
        private ImageView img1,img2,img3;
        private Button btn1,btn2,btn3;

        public CardViewHolder(View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.IV_Parking1);
            img2 = itemView.findViewById(R.id.IV_Parking2);
            img3 = itemView.findViewById(R.id.IV_Parking3);
            txt_title = itemView.findViewById(R.id.txt_card);
            txt_camera = itemView.findViewById(R.id.txt_camera);
            btn1=itemView.findViewById(R.id.btn_chg_parking1);
            btn2=itemView.findViewById(R.id.btn_chg_parking2);
            btn3=itemView.findViewById(R.id.btn_chg_parking3);
        }

        public void bind(CardItem cardItem) {
            txt_title.setText(cardItem.getCardText());
            txt_camera.setText(cardItem.getSelectedCamera());

            setImageResourceBasedOnCardP(img1, cardItem.getCardP1());
            setImageResourceBasedOnCardP(img2, cardItem.getCardP2());
            setImageResourceBasedOnCardP(img3, cardItem.getCardP3());
            if(txt_camera!=null){
                DisableButton(btn1,txt_camera,cardItem.getSelectedCamera());
                DisableButton(btn2,txt_camera,cardItem.getSelectedCamera());
                DisableButton(btn3,txt_camera,cardItem.getSelectedCamera());
            }

        }
    }
    private void DisableButton(Button button,TextView textView,String camera){
        if (camera == null || camera.isEmpty()) {
            button.setEnabled(false);
        } else {
            if (textView != null) {
                textView.setText(camera);
            }
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
}