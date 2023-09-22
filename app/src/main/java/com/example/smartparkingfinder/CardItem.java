package com.example.smartparkingfinder;

public class CardItem {
    private String cardId;
    private String cardText;
    private String selectedCamera;
    private String cardP1; // New field
    private String cardP2; // New field
    private String cardP3; // New field
    private String spaceP1;
    public CardItem(String cardId, String cardText) {
        this.cardId = cardId;
        this.cardText = cardText;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
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
    public String getSpaceP1() {
        return spaceP1;
    }


}
