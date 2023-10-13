package com.example.smartparkingfinder;

public class CameraModel {
    private String cameraName;
    private String locationName;
    private String floorName;
    private String cardName;
    private String status;
    private String userID;

    public CameraModel() {
        // Default constructor required for Firebase
    }

    public CameraModel(String cameraName, String locationName, String floorName, String cardName, String status, String userID) {
        this.cameraName = cameraName;
        this.locationName = locationName;
        this.floorName = floorName;
        this.cardName = cardName;
        this.status = status;
        this.userID = userID;
    }

    // Getters and setters for each property

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}