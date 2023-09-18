package com.example.smartparkingfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
                int tabCount = tabLayout.getTabCount();
                // Add a new tab and fragment here
                addNewTab(tabCount);
                saveTabToFirebase(locationRef,"Floor "+tabCount);
                Log.d("Database","Saved Tab");

            }
        });
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                Fragment currentFragment = adapter.getItem(currentItem);
                // Get the currently selected tab's position
                int selectedTabPosition = viewPager.getCurrentItem();
                // Get the tab title based on the selected tab position
                String selectedTabTitle = "Floor "+ (selectedTabPosition);
                if (currentFragment instanceof TestFragment) {
                    ((TestFragment) currentFragment).addCardToRecyclerView("NEW!");
                    saveCardToFirebase(locationRef,selectedTabTitle,"Card 1");
                    Log.d("Database","Saved Card");
                }
            }
        });
    }
    private void addTabFromFirebase(String tabId) {
        // Create a new fragment for the tab content and add it to the adapter
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString("tabTitle", tabId); // Use the retrieved tab ID as the tab title
        fragment.setArguments(args);

        // Add the fragment to the adapter and notify the adapter of the data set change
        adapter.addFragment(fragment, tabId);
        adapter.notifyDataSetChanged();
    }

    private void addNewTab(int Count) {
        // Create a new fragment for the tab content and add it to the adapter
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        count++;
        args.putString("tabTitle", "Tab "+ count); // Replace "Tab 1" with the actual tab title
        viewPager.setOffscreenPageLimit(Count);
        fragment.setArguments(args);// Generate a unique tab title
        adapter.addFragment(fragment, "Floor "+count);
        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();

    }

    private void saveTabToFirebase(DatabaseReference locationRef, String tabTitle) {
        // Set the tab title as the key in Firebase
        DatabaseReference tabRef = locationRef.child("details").child("layout").child(tabTitle);
        // Save the tab title
        tabRef.child("name").setValue(tabTitle);
    }
    private void saveCardToFirebase(DatabaseReference locationRef, String currentTabId, String cardText) {
        // Get a reference to the "cards" section under the selected tab
        DatabaseReference cardsRef = locationRef.child("details").child("layout")
                .child(currentTabId).child("card");
        // Generate a unique card ID using push()
        DatabaseReference cardRef = cardsRef.push();
        cardRef.child("text").setValue(cardText);
        Log.d("saveCard",cardsRef.toString());
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
                    // Process each tab ID
                    addTabFromFirebase(tabId);

                    // Load card data for the tab
                    loadCardDataFromFirebase(locationRef, tabId);
                }
                viewPager.setOffscreenPageLimit(tabCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadCardDataFromFirebase(DatabaseReference locationRef, String tabTitle) {
        DatabaseReference cardsRef = locationRef.child("details").child("layout").child(tabTitle).child("card");
        cardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                    String cardText = cardSnapshot.child("text").getValue(String.class);
                    // Here, you have the cardText, you can add it to the corresponding fragment
                    updateFragmentUI(tabTitle, cardText);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateFragmentUI(String tabTitle, String cardText) {
        for (int i = 0; i < adapter.getCount(); i++) {
            Fragment fragment = adapter.getItem(i);
            String fragmentTitle = adapter.getPageTitle(i).toString();
            if (fragment instanceof TestFragment && fragmentTitle.equals(tabTitle)) {
                ((TestFragment) fragment).addCardToRecyclerView(cardText);
                break;
            }
        }
    }


}