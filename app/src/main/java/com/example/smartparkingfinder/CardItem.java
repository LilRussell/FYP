package com.example.smartparkingfinder;
public class CardItem {
    private String cardId; // Add a field for the card ID
    private String cardText;
    private String selectedCamera;

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
}
