package com.example.smartparkingfinder;

public class locationRVModel {
    private String id;
    private String name;
    private String description;

    public locationRVModel() {
        // Default constructor required for Firebase
    }

    public locationRVModel(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
}
