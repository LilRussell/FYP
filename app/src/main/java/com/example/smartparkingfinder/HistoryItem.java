package com.example.smartparkingfinder;

public class HistoryItem {
    private String userId;
    private String carName;
    private String cardName;
    private String fragmentName;
    private String timestamp;
    private String location;
    public HistoryItem() {
        // Default constructor required for Firebase
    }

    public HistoryItem(String userId, String carName, String cardName, String fragmentName,String location, String timestamp) {
        this.userId = userId;
        this.carName = carName;
        this.cardName = cardName;
        this.fragmentName = fragmentName;
        this.timestamp = timestamp;
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String location) {
        this.location = location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getLocation() {
        return location;
    }


}

