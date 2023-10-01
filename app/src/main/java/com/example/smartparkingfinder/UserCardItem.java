package com.example.smartparkingfinder;

public class UserCardItem {
    private String cardId;
    private String cardText;
    private String selectedCamera;
    private String cardP1;
    private String cardP2;
    private String cardP3;
    private String statusP1;
    private String statusP2;
    private String statusP3;
    private boolean isHighlighted = false;
    public UserCardItem(String cardId, String cardText) {
        this.cardId = cardId;
        this.cardText = cardText;
    }

    public String getCardId() {
        return cardId;
    }


    public String getCardText() {
        return cardText;
    }


    public String getSelectedCamera() {
        return selectedCamera;
    }

    public void setSelectedCamera(String selectedCamera) {
        this.selectedCamera = selectedCamera;
    }

    // Getter and Setter for cardP1
    public String getCardP1() {
        return cardP1;
    }

    public void setCardP1(String cardP1) {
        this.cardP1 = cardP1;
    }

    // Getter and Setter for cardP2
    public String getCardP2() {
        return cardP2;
    }

    public void setCardP2(String cardP2) {
        this.cardP2 = cardP2;
    }

    // Getter and Setter for cardP3
    public String getCardP3() {
        return cardP3;
    }

    public void setCardP3(String cardP3) {
        this.cardP3 = cardP3;
    }
    public String getStatusP1() {
        return statusP1;
    }

    public String getStatusP2() {
        return statusP2;
    }
    public String getStatusP3() {
        return statusP3;
    }
    public void setStatusP1(String statusP1){
        this.statusP1=statusP1;
    }
    public void setStatusP2(String statusP2){
        this.statusP2=statusP2;
    }
    public void setStatusP3(String statusP3){
        this.statusP3=statusP3;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }


}
