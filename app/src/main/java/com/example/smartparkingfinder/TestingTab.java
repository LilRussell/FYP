package com.example.smartparkingfinder;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestingTab extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView txtNothing;
    private TestTabAdapter adapter;
    private Button addTabButton,addCardButton,deleteTabButton;
    private String locationId,locationName;
    private String adminId;
    private Toolbar toolbar;
    private int count=0;
    private int tabCount=0;
    private List<TabInfo> tabInfoList = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_tab);
        toolbar = findViewById(R.id.toolbar);
        locationName = getIntent().getStringExtra("locationName").toUpperCase(Locale.ROOT);
        Log.d("title",locationName);
        toolbar.setTitle(locationName);
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
        addTabButton = findViewById(R.id.addTabButton);
        addCardButton = findViewById(R.id.addParkingBox);
        deleteTabButton = findViewById(R.id.deleteTabButton);
        txtNothing = findViewById(R.id.txt_nothing);
        // Load tab data and cards for each tab from Firebase
        loadTabsAndCardsFromFirebase(locationRef);
        if(adapter.getCount()>0){
            txtNothing.setVisibility(View.GONE);
        }else{
            txtNothing.setVisibility(View.VISIBLE);
        }
        addTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the context from the clicked view
                Context context = view.getContext();
                int tabCount = tabLayout.getTabCount();
                // Create an AlertDialog to input the tab title
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(TestingTab.this,R.style.CustomAlertDialogTheme));
                builder.setTitle("Enter Floor Name");

                // Set up the input view
                final EditText input = new EditText(context);
                input.setTextColor(getResources().getColor(R.color.black));
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

                            Log.d("Database", "Saved Tab");



                        } else {
                            // Handle the case where the input is empty
                            Toast.makeText(context, "Floor Name cannot be Empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
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
                // Show the AlertDialog
                dialog.show();
            }
        });
        addCardButton.setOnClickListener(new View.OnClickListener() {
            private int cardCounter = 1; // Initialize a counter

            @Override
            public void onClick(View v) {
                int currentItem = viewPager.getCurrentItem();
                Fragment currentFragment = adapter.getItem(currentItem);

                // Check if there are tabs added

                    if (currentFragment instanceof TestFragment) {

                            // Get the TabInfo object for the currently selected tab
                            TabInfo selectedTabInfo = tabInfoList.get(currentItem);
                            cardCounter++;
                            // Save the new card to Firebase using the tab ID from TabInfo
                            saveCardToFirebase(locationRef, selectedTabInfo.getId(), cardCounter, currentFragment);
                            Log.d("Database", "Saved Card");
                }
            else {
                    Toast.makeText(TestingTab.this, "No Floor added yet. Please add a Floor first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        deleteTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = viewPager.getCurrentItem();
                deleteTabAndUpdateViewPager(currentItem);

            }
        });


    }
    private void deleteTabAndUpdateViewPager(int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("location").child(locationId);
        if (position >= 0 && position < tabInfoList.size()) {
            String currentTabId = tabInfoList.get(position).getId();
            if (currentTabId != null) {
                // Create a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(TestingTab.this,R.style.CustomAlertDialogTheme));
                builder.setTitle("Confirm Deletion");
                builder.setMessage("Are you sure you want to delete this Floor?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed deletion


                        updateCamerasForDeletedTab(currentTabId);
                        deleteTabFromFirebase(locationRef, currentTabId);
                        // Clear all fragments from the ViewPager
                        tabLayout.removeAllTabs();
                        tabLayout.clearOnTabSelectedListeners();
                        adapter.clearFragments();
                        tabInfoList.clear();
                        tabCount--; // Decrement the fragment count
                        if (tabCount==0) {
                            txtNothing.setVisibility(View.VISIBLE);
                            addCardButton.setVisibility(View.GONE);
                        } else {
                            txtNothing.setVisibility(View.GONE);
                            addCardButton.setVisibility(View.VISIBLE);
                        }
                        viewPager.setOffscreenPageLimit(tabCount);
                        adapter = new TestTabAdapter(getSupportFragmentManager());
                        viewPager.setAdapter(adapter);
                        tabLayout.setupWithViewPager(viewPager);
                        loadTabsAndCardsFromFirebase(locationRef);

                        TabLayout.Tab tabToRemove = tabLayout.getTabAt(position);
                        if (tabToRemove != null) {
                            tabLayout.removeTab(tabToRemove);
                        }
                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                        if (position < adapter.getCount()) {
                            viewPager.setCurrentItem(position);
                        } else if (position > 0) {
                            viewPager.setCurrentItem(position - 1);
                        }
                        for (TabInfo tabInfo : tabInfoList) {
                            tabLayout.addTab(tabLayout.newTab().setText(tabInfo.getTitle()));
                        }
                        Toast.makeText(TestingTab.this, "Floor Deleted.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked No, do nothing
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
                // Show the AlertDialog
                dialog.show();
            } else {
                // Handle the case where currentTabId is null
                Toast.makeText(TestingTab.this, "Tab ID is null.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where currentItem is out of bounds

            Toast.makeText(TestingTab.this, "No Floor Found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTabFromFirebase(DatabaseReference locationRef, String tabId) {
        // Remove the tab node using its unique ID
        locationRef.child("details").child("layout").child(tabId).removeValue();
    }
    private void updateCamerasForDeletedTab(String tabId) {
        DatabaseReference cameraRef = FirebaseDatabase.getInstance().getReference().child("camera");
        cameraRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cameraSnapshot : dataSnapshot.getChildren()) {
                    String assignedTab = cameraSnapshot.child("assignedTab").getValue(String.class);
                    if (assignedTab != null && assignedTab.equals(tabId)) {
                        // Update the camera's assignedCard and assignedLocation to "None"
                        String cameraId = cameraSnapshot.getKey();
                        updateCameraInFirebase(cameraId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateCameraInFirebase(String cameraId) {
        DatabaseReference cameraRef = FirebaseDatabase.getInstance().getReference().child("camera").child(cameraId);
        cameraRef.child("assignedCard").setValue("None");
        cameraRef.child("assignedLocation").setValue("None");
        cameraRef.child("assignedTab").setValue("None");
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
        String adminIdToCard = adminId; // Replace with the actual admin ID
        args.putString("adminIdKey", adminIdToCard);
        fragment.setArguments(args);

        // Add the fragment to the adapter and notify the adapter of the data set change
        adapter.addFragment(fragment, tabTitle);
        fragments.add(fragment);
        adapter.notifyDataSetChanged();
        if (adapter.getCount()>0) {
            txtNothing.setVisibility(View.GONE);
            addCardButton.setVisibility(View.VISIBLE);
        }
    }

    private void addNewTab(int Count, String title) {
        // Check if a tab with the same title exists
        boolean isTabTitleExists = false;
        for (TabInfo tabInfo : tabInfoList) {
            if (tabInfo.getTitle().equals(title)) {
                isTabTitleExists = true;
                break;
            }
        }

        if (isTabTitleExists) {
            // Show an error message or handle the case where a similar tab title exists
            Toast.makeText(this, "Floor Name already exists", Toast.LENGTH_SHORT).show();
        } else {
            // Create a new tab if the title is unique
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference locationRef = database.getReference("location").child(locationId);
            TestFragment fragment = new TestFragment();
            viewPager.setOffscreenPageLimit(Count);
            adapter.addFragment(fragment, title);
            saveTabToFirebase(locationRef, title);
            fragments.add(fragment);
            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged();
            if (adapter.getCount() > 0) {
                addCardButton.setVisibility(View.VISIBLE);
            }
        }
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
    private void saveCardToFirebase(DatabaseReference locationRef, String currentTabId, int CardCount,Fragment currentFragment) {
        // Get a reference to the "cards" section under the selected tab
        DatabaseReference cardsRef = locationRef.child("details").child("layout")
                .child(currentTabId).child("card");

        // Generate a unique card ID using push()
        DatabaseReference cardRef = cardsRef.push();

        // Get the unique ID generated by push()
        String cardId = cardRef.getKey();
        // Create a new CardItem with default or empty values
        CardItem newCard = new CardItem(cardId, "Card " + CardCount);
        // Set the card data, including the unique ID
        newCard.setCardId(cardId);
        // Set all properties to default or empty values
        newCard.setSelectedCamera("");
        newCard.setCardP1("");
        newCard.setCardP2("");
        newCard.setCardP3("");
        newCard.setStatusP1("");
        newCard.setStatusP2("");
        newCard.setStatusP3("");


        // Save the cardItem to Firebase
        cardRef.setValue(newCard);

        ((TestFragment) currentFragment).addCardToRecyclerView(newCard);
        Bundle args = new Bundle();
        args.putString("locationId", locationId); // Pass the locationId
        args.putString("tabTitle", currentTabId); // Generate a unique tab title
        String adminIdToCard = adminId; // Replace with the actual admin ID
        args.putString("adminIdKey", adminIdToCard);
        currentFragment.setArguments(args);
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
                if (adapter.getCount()==0) {
                    addCardButton.setVisibility(View.GONE);
                } else {
                    addCardButton.setVisibility(View.VISIBLE);
                }
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
                    String selectedCamera = cardSnapshot.child("selectedCamera").getValue(String.class);
                    String cardP1 = cardSnapshot.child("cardP1").getValue(String.class);
                    String cardP2 = cardSnapshot.child("cardP2").getValue(String.class);
                    String cardP3 = cardSnapshot.child("cardP3").getValue(String.class);
                    String statusP1 =cardSnapshot.child("statusP1").getValue(String.class);
                    String statusP2 =cardSnapshot.child("statusP2").getValue(String.class);
                    String statusP3 =cardSnapshot.child("statusP3").getValue(String.class);
                    // Create a CardItem object with the retrieved data
                    CardItem cardItem = new CardItem(cardId, cardText);
                    cardItem.setSelectedCamera(selectedCamera);
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