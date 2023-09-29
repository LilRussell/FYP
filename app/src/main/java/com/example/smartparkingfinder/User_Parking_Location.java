package com.example.smartparkingfinder;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class User_Parking_Location extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private UserTabAdapter adapter;
    private Button addTabButton,addCardButton;
    private String locationId,locationName;

    private Toolbar toolbar;
    private int count=0;
    private int tabCount=0;
    private List<TabInfo> tabInfoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_parking_location);
        toolbar = findViewById(R.id.toolbar);

        locationId = getIntent().getStringExtra("locationId");
        locationName = getIntent().getStringExtra("locationName");
        Log.d("title",locationName);
        toolbar.setTitle(locationName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout = findViewById(R.id.tabLayout_test);
        viewPager = findViewById(R.id.viewPager_test);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("location").child(locationId);
        adapter = new UserTabAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        addTabButton = findViewById(R.id.addTab_test);
        addCardButton = findViewById(R.id.addCard_test);
        // Load tab data and cards for each tab from Firebase
        loadTabsAndCardsFromFirebase(locationRef);

    }
    private void addTabFromFirebase(String tabId, String tabTitle) {
        // Create a TabInfo object and add it to the list
        TabInfo tabInfo = new TabInfo(tabId, tabTitle);
        tabInfoList.add(tabInfo);

        // Create a new fragment for the tab content and add it to the adapter
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString("locationId", locationId); // Pass the locationId
        args.putString("tabTitle", tabId); // Generate a unique tab title
        fragment.setArguments(args);

        // Add the fragment to the adapter and notify the adapter of the data set change
        adapter.addFragment(fragment, tabTitle);
        adapter.notifyDataSetChanged();
    }

    private void loadTabsAndCardsFromFirebase(DatabaseReference locationRef) {
        // Retrieve Tab IDs
        locationRef.child("details").child("layout").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tabSnapshot : dataSnapshot.getChildren()) {
                    tabCount = (int) dataSnapshot.getChildrenCount();
                    String tabId = tabSnapshot.getKey();
                    Log.d("tabid",tabId);
                    // Load card data for the tab


                    // Retrieve the value under .child("name")
                    String tabName = tabSnapshot.child("name").getValue(String.class);
                    String tabID = tabSnapshot.child("id").getValue(String.class);
                    if (tabName != null) {
                        // Do something with the tabName
                        addTabFromFirebase(tabID,tabName);
                        loadCardDataFromFirebase(locationRef, tabId,tabName);
                        Log.d("Tab Name", tabName);
                    }
                }
                viewPager.setOffscreenPageLimit(tabCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadCardDataFromFirebase(DatabaseReference locationRef,String tabID, String tabTitle) {
        DatabaseReference cardsRef = locationRef.child("details").child("layout").child(tabID).child("card");
        cardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                    String cardId = cardSnapshot.getKey(); // Get the unique card ID
                    String cardText = cardSnapshot.child("cardText").getValue(String.class);
                    String cardCamera = cardSnapshot.child("selectedCamera").getValue(String.class);
                    String cardP1 = cardSnapshot.child("cardP1").getValue(String.class);
                    String cardP2 = cardSnapshot.child("cardP2").getValue(String.class);
                    String cardP3 = cardSnapshot.child("cardP3").getValue(String.class);
                    String statusP1 =cardSnapshot.child("statusP1").getValue(String.class);
                    String statusP2 =cardSnapshot.child("statusP2").getValue(String.class);
                    String statusP3 =cardSnapshot.child("statusP3").getValue(String.class);
                    // Create a CardItem object with the retrieved data
                    UserCardItem cardItem = new UserCardItem(cardId, cardText);
                    cardItem.setSelectedCamera(cardCamera);
                    cardItem.setCardP1(cardP1);
                    cardItem.setCardP2(cardP2);
                    cardItem.setCardP3(cardP3);
                    cardItem.setStatusP1(statusP1);
                    cardItem.setStatusP2(statusP2);
                    cardItem.setStatusP3(statusP3);
                    updateFragmentUI(tabTitle, cardItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateFragmentUI(String tabTitle, UserCardItem cardItem) {
        for (int i = 0; i < adapter.getCount(); i++) {
            Fragment fragment = adapter.getItem(i);
            String fragmentTitle = adapter.getPageTitle(i).toString();
            if (fragment instanceof UserFragment && fragmentTitle.equals(tabTitle)) {
                ((UserFragment) fragment).addCardToRecyclerView(cardItem);
                break;
            }
        }
    }


}