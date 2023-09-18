package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingLayout extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyPagerAdapter adapter;
    private Button addTabButton;
    private Button addParkingBox;
    private ImageView leftIndicator;
    private ImageView rightIndicator;

    private List<String> tabTitles = new ArrayList<>();
    private List<Fragment> tabFragments = new ArrayList<>();
    private int tabCount = 0; // To keep track of the number of tabs
    private String locationId;
    private boolean isFirstLoad = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_layout);
        locationId = getIntent().getStringExtra("locationId");
        Log.d("CheckID", locationId);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        addTabButton = findViewById(R.id.addTabButton);
        addParkingBox = findViewById(R.id.addParkingBox);
        leftIndicator = findViewById(R.id.leftIndicator);
        rightIndicator = findViewById(R.id.rightIndicator);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("location").child(locationId);

        adapter = new MyPagerAdapter(getSupportFragmentManager());


        // Initially, hide the scroll indicators
        leftIndicator.setVisibility(View.GONE);
        rightIndicator.setVisibility(View.GONE);
        // Listen for changes to tabs in Firebase and load the tabs
        locationRef.child("details").child("layout").child("tabs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> tabSnapshots = dataSnapshot.getChildren();
                int tabCount = 0;

                // Iterate through the tabSnapshots to count the number of tabs
                for (DataSnapshot tabSnapshot : tabSnapshots) {
                    // Each child under "tabs" represents a tab, so increment the tabCount
                    tabCount++;
                }

                if (isFirstLoad) {
                    // Update your tab count in the activity
                    updateTabCount(tabCount);

                    if (tabCount > 0) {
                        // Load the tabs based on the retrieved tab count
                        loadTabsFromFirebase(locationRef);

                        viewPager.setOffscreenPageLimit(tabCount);
                    }

                    isFirstLoad = false; // Set the flag to false after the first load
                }
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during the database query
                Log.e("FirebaseError", "Failed to retrieve tabs: " + databaseError.getMessage());
            }
        });




        addTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Increment the tab count and generate the tab title
                tabCount++;
                String newTabTitle = "Floor " + tabCount;
                tabTitles.add(newTabTitle);

                // Create a new fragment and add it to the adapter
                PageFragment newFragment = new PageFragment();
                adapter.addFragment(newFragment, newTabTitle);
                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged();

                // Select the newly added tab to make it visible
                int newTabIndex = adapter.getCount() - 1;
                TabLayout.Tab selectedTab = tabLayout.getTabAt(newTabIndex);
                if (selectedTab != null) {
                    selectedTab.select();
                } else {
                    // Handle the case where the selectedTab is null (e.g., if the TabLayout is not properly initialized)
                    Log.e("TabSelectionError", "Selected tab is null.");
                }

                // Update scroll indicators after adding a new tab
                updateScrollIndicators();
                // Save the new tab as a unique child under "layout"
                saveTabToFirebase(locationRef, newTabTitle);
                // Log statement added here
                Log.d("FragmentCreation", "New tab created: " + newTabTitle);
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                // Update scroll indicators when a tab is selected
                updateScrollIndicators();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // The 'position' parameter indicates the index of the selected tab
                Log.d("TabSelected", "Selected tab index: " + position);

                // Retrieve the fragment associated with the selected tab
                PageFragment selectedFragment = (PageFragment) adapter.getItem(position);

                if (selectedFragment != null) {
                    // Check if the fragment is newly created
                    if (selectedFragment.isFragmentCreated) {
                        Log.d("FragmentSelected", "Newly created fragment");
                        // Perform actions specific to a newly created fragment
                        selectedFragment.isFragmentCreated = false; // Reset the flag
                    } else {
                        Log.d("FragmentSelected", "Fragment was not recreated");
                        // Perform actions for a fragment that was not recreated
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        addParkingBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the currently selected tab's position
                int selectedTabPosition = viewPager.getCurrentItem();

                // Get the tab title based on the selected tab position
                String selectedTabTitle = "Floor "+ (selectedTabPosition+1);
                Log.d("Tab", selectedTabTitle);

                // Check if the PageFragment is attached to an activity
                PageFragment selectedFragment = (PageFragment) adapter.getItem(selectedTabPosition);

                    // Retrieve the tab ID from Firebase based on the selected tab title
                    DatabaseReference tabsRef = locationRef.child("details").child("layout").child("tabs");
                    tabsRef.child(selectedTabTitle).equalTo(selectedTabTitle).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("tabsRef","OK");
                            // Get the card text (e.g., "Pillar 1", "Pillar 2")
                            String cardText = "Pillar 1";
                            // Call the saveCardToFirebase function to save the card
                            saveCardToFirebase(locationRef, selectedTabTitle, cardText);
                            // Update the fragment's UI to reflect the new card
                            PageFragment selectedFragment = (PageFragment) adapter.getItem(selectedTabPosition);
                            //selectedFragment.addCardViewToContainer(cardText);
                            Log.d("TabRef", tabsRef.toString());


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors that may occur during the database query
                            Log.e("FirebaseError", "Failed to retrieve tab ID: " + databaseError.getMessage());
                        }
                    });

            }
        });


    }
    private void updateScrollIndicators() {
        // Check if the TabLayout is scrollable
        boolean isScrollable = isTabLayoutScrollable(tabLayout);
        // Log the scrollability status
        // Show/hide the indicators based on scrollability
        leftIndicator.setVisibility(isScrollable ? View.VISIBLE : View.GONE);
        rightIndicator.setVisibility(isScrollable ? View.VISIBLE : View.GONE);
    }

    private boolean isTabLayoutScrollable(TabLayout tabLayout) {
        int tabLayoutWidth = tabLayout.getWidth();
        int totalTabWidth = 0;

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tabView = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            tabView.measure(0, 0);
            totalTabWidth += tabView.getMeasuredWidth();
        }

        return totalTabWidth > tabLayoutWidth;
    }

    private void updateTabCount(int newTabCount) {
        tabCount = newTabCount;
        // Update your UI with the new tab count if needed
        // For example, if you have a TabLayout named tabLayout:
        // tabLayout.setTabCount(tabCount);
    }

    private void saveTabToFirebase(DatabaseReference locationRef, String tabTitle) {
        // Set the tab title as the key in Firebase
        DatabaseReference tabRef = locationRef.child("details").child("layout").child("tabs").child(tabTitle);

        // Save the tab title
        tabRef.child("name").setValue(tabTitle);
    }
    private void saveCardToFirebase(DatabaseReference locationRef, String currentTabId, String cardText) {
        // Get a reference to the "cards" section under the selected tab
        DatabaseReference cardsRef = locationRef.child("details").child("layout")
                .child("tabs").child(currentTabId).child("card");
        Log.d("saveCard",cardsRef.toString());
        // Generate a unique card ID using push()
        DatabaseReference cardRef = cardsRef.push();

        // Set the card text under the generated card ID
        cardRef.child("text").setValue(cardText);
    }
    private void loadTabsFromFirebase(DatabaseReference locationRef) {
        locationRef.child("details").child("layout").child("tabs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int tabCount = (int) dataSnapshot.getChildrenCount(); // Get the number of tabs from Firebase

                // Clear existing tabs before adding new ones
                tabTitles.clear();
                tabFragments.clear();

                for (DataSnapshot tabSnapshot : dataSnapshot.getChildren()) {
                    // Get the tab title (name) from Firebase
                    String tabTitle = tabSnapshot.child("name").getValue(String.class);

                    // Create a new fragment and add it to the adapter
                    PageFragment newFragment = new PageFragment();
                    adapter.addFragment(newFragment, tabTitle);
                    tabFragments.add(newFragment);

                    // Load card data for this tab
                    loadCardDataForTab(tabSnapshot.getRef(), newFragment);

                    // Add the tab title to your list
                    tabTitles.add(tabTitle);

                    // Log statement added here
                    Log.d("FragmentCreation", "Fragment created: " + tabTitle);
                }

                // Set the tab count in your activity
                updateTabCount(tabCount);

                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during the database query
                Log.e("FirebaseError", "Failed to retrieve tabs: " + databaseError.getMessage());
            }
        });
    }
    private void loadCardDataForTab(DatabaseReference tabRef, PageFragment fragment) {
        tabRef.child("card").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through the card data for this tab
                for (DataSnapshot cardSnapshot : dataSnapshot.getChildren()) {
                    // Get the card text from Firebase
                    String cardText = cardSnapshot.child("text").getValue(String.class);

                    // Load the card data into the corresponding fragment
                   if( fragment.getView()!=null){
                    //   fragment.addCardViewToContainer(cardText);
                   }
                   else{
                       Log.d("CV","View is Null");
                   }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during the database query
                Log.e("FirebaseError", "Failed to retrieve card data: " + databaseError.getMessage());
            }
        });
    }
}