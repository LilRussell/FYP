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

public class TestingTab extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TestTabAdapter adapter;
    private Button addTabButton,addCardButton;
    private String locationId;
    private String adminId;
    private Toolbar toolbar;
    private int count=0;
    private int tabCount=0;
    private List<TabInfo> tabInfoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_tab);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        locationId = getIntent().getStringExtra("locationId");
        adminId = getIntent().getStringExtra("adminId");
        Log.d("CheckID", locationId);
        tabLayout = findViewById(R.id.tabLayout_test);
        viewPager = findViewById(R.id.viewPager_test);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("location").child(locationId);
        adapter = new TestTabAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        addTabButton = findViewById(R.id.addTab_test);
        addCardButton = findViewById(R.id.addCard_test);
        // Load tab data and cards for each tab from Firebase
        loadTabsAndCardsFromFirebase(locationRef);
        addTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the context from the clicked view
                Context context = view.getContext();
                int tabCount = tabLayout.getTabCount();
                // Create an AlertDialog to input the tab title
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Tab Title");

                // Set up the input view
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the positive and negative buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tabTitle = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(tabTitle)) {
                            // Add a new tab and fragment with the entered tab title
                            addNewTab(tabCount,tabTitle);
                            saveTabToFirebase(locationRef, tabTitle);
                            Log.d("Database", "Saved Tab");



                        } else {
                            // Handle the case where the input is empty
                            Toast.makeText(context, "Tab title cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Show the AlertDialog
                builder.show();
            }
        });
         addCardButton.setOnClickListener(new View.OnClickListener() {
            private int cardCounter = 1; // Initialize a counter

            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                Fragment currentFragment = adapter.getItem(currentItem);

                if (currentFragment instanceof TestFragment) {
                    // Get the TabInfo object for the currently selected tab
                    TabInfo selectedTabInfo = tabInfoList.get(currentItem);

                    // Create a new CardItem with default or empty values
                    CardItem newCard = new CardItem(null, "Card " + cardCounter);

                    // Set all properties to default or empty values
                    newCard.setSelectedCamera("");
                    newCard.setCardP1("");
                    newCard.setCardP2("");
                    newCard.setCardP3("");

                    // Increment the card counter for the next card
                    cardCounter++;

                    // Add the new card to the RecyclerView and the list
                    ((TestFragment) currentFragment).addCardToRecyclerView(newCard);

                    // Save the new card to Firebase using the tab ID from TabInfo
                    saveCardToFirebase(locationRef, selectedTabInfo.getId(), newCard);
                    Log.d("Database", "Saved Card");
                }
            }
        });
    }
    private void addTabFromFirebase(String tabId, String tabTitle) {
        // Create a TabInfo object and add it to the list
        TabInfo tabInfo = new TabInfo(tabId, tabTitle);
        tabInfoList.add(tabInfo);

        // Create a new fragment for the tab content and add it to the adapter
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString("locationId", locationId); // Pass the locationId
        args.putString("tabTitle", tabId); // Generate a unique tab title
        fragment.setArguments(args);

        // Add the fragment to the adapter and notify the adapter of the data set change
        adapter.addFragment(fragment, tabTitle);
        adapter.notifyDataSetChanged();
    }

    private void addNewTab(int Count,String title) {
        // Create a new fragment for the tab content and add it to the adapter
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString("locationId", locationId); // Pass the locationId
        args.putString("tabTitle", title); // Generate a unique tab title
        fragment.setArguments(args); // Replace "Tab 1" with the actual tab title
        viewPager.setOffscreenPageLimit(Count);
        fragment.setArguments(args);// Generate a unique tab title
        adapter.addFragment(fragment, title);
        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();

    }

    private void saveTabToFirebase(DatabaseReference locationRef, String tabTitle) {
        // Set the tab title as the key in Firebase
        DatabaseReference tabRef = locationRef.child("details").child("layout");
        DatabaseReference tabsRef = tabRef.push();
        // Save the tab title
        // Get the unique ID generated by push()
        String tabid = tabsRef.getKey();
        tabsRef.child("id").setValue(tabid);
        tabsRef.child("name").setValue(tabTitle);
        TabInfo tabInfo = new TabInfo(tabid, tabTitle);
        tabInfoList.add(tabInfo);
    }
    private void saveCardToFirebase(DatabaseReference locationRef, String currentTabId, CardItem cardItem) {
        // Get a reference to the "cards" section under the selected tab
        DatabaseReference cardsRef = locationRef.child("details").child("layout")
                .child(currentTabId).child("card");

        // Generate a unique card ID using push()
        DatabaseReference cardRef = cardsRef.push();

        // Get the unique ID generated by push()
        String cardId = cardRef.getKey();

        // Set the card data, including the unique ID
        cardItem.setCardId(cardId);

        // Save the cardItem to Firebase
        cardRef.setValue(cardItem);

        Log.d("saveCard", cardsRef.toString());
    }
    private void updateTabCount(int newTabCount) {
        tabCount = newTabCount;

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
                    String cardText = cardSnapshot.child("text").getValue(String.class);
                    String selectedCamera = cardSnapshot.child("selectedCamera").getValue(String.class);
                    String cardP1 = cardSnapshot.child("cardP1").getValue(String.class);
                    String cardP2 = cardSnapshot.child("cardP2").getValue(String.class);
                    String cardP3 = cardSnapshot.child("cardP3").getValue(String.class);

                    // Create a CardItem object with the retrieved data
                    CardItem cardItem = new CardItem(cardId, cardText);
                    cardItem.setSelectedCamera(selectedCamera);
                    cardItem.setCardP1(cardP1);
                    cardItem.setCardP2(cardP2);
                    cardItem.setCardP3(cardP3);

                    // Here, you have the cardItem, you can add it to the corresponding fragment
                    updateFragmentUI(tabTitle, cardItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateFragmentUI(String tabTitle, CardItem cardItem) {
        for (int i = 0; i < adapter.getCount(); i++) {
            Fragment fragment = adapter.getItem(i);
            String fragmentTitle = adapter.getPageTitle(i).toString();
            if (fragment instanceof TestFragment && fragmentTitle.equals(tabTitle)) {
                ((TestFragment) fragment).addCardToRecyclerView(cardItem);
                break;
            }
        }
    }


}