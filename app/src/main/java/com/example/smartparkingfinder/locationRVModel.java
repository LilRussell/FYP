package com.example.smartparkingfinder;

public class locationRVModel {
    private String id;
    private String name;
    private String description;
    private int parkingAvailability; // Add a new field for parking number
    private String imageURL; // Add a field for image URL

    public locationRVModel() {
        // Default constructor required for Firebase
    }

    public locationRVModel(String id, String name, String description,int parkingAvailability, String imageURL) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parkingAvailability = parkingAvailability;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public int getParkingNumber() {
        return parkingAvailability;
    }

    public String getImageURL() {
        return imageURL;
    }
    // Setter method for imageURL
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    // Getter method for parking availability
    public int getParkingAvailability() {
        return parkingAvailability; // Assuming parkingNumber represents parking availability
    }
}
