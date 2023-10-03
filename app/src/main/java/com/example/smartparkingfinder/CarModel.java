package com.example.smartparkingfinder;

public class CarModel {
    private String userID;
    private String numberplate;
    private String model;
    private String CarID;
    private boolean isDefault;
    public CarModel() {
        // Default constructor required by Firebase
    }
    // Constructor
    public CarModel(String CarID,String userID, String numberplate, String model, boolean isDefault) {
        this.CarID = CarID;
        this.userID = userID;
        this.numberplate = numberplate;
        this.model = model;
        this.isDefault = isDefault;
    }
    // Getter method for userID
    public String getCarID() {
        return CarID;
    }

    // Setter method for userID
    public void setCarID(String carID) {
        this.CarID = carID;
    }

    // Getter method for userID
    public String getUserID() {
        return userID;
    }

    // Setter method for userID
    public void setUserID(String userID) {
        this.userID = userID;
    }

    // Getter method for numberplate
    public String getNumberplate() {
        return numberplate;
    }

    // Setter method for numberplate
    public void setNumberplate(String numberplate) {
        this.numberplate = numberplate;
    }

    // Getter method for model
    public String getModel() {
        return model;
    }

    // Setter method for model
    public void setModel(String model) {
        this.model = model;
    }

    // Getter method for default
    public boolean isDefault() {
        return isDefault;
    }

    // Setter method for default
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
