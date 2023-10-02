package com.example.smartparkingfinder;

public class TabInfo {
    private String id;
    private String title;
    private int position;
    public TabInfo() {
        // Default constructor required for Firebase
    }

    public TabInfo(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
