package com.example.smartparkingfinder;
public class CardItem {
    private String cardText;
    private String selectedCamera;
    public CardItem(String cardText) {
        this.cardText = cardText;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }
}
