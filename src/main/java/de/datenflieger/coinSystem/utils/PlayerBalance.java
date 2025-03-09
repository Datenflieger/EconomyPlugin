package de.datenflieger.coinSystem.utils;

public class PlayerBalance {

    private final String playerName;
    private final double balance;

    public PlayerBalance(String playerName, double balance) {
        this.playerName = playerName;
        this.balance = balance;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getBalance() {
        return balance;
    }

    public String getFormattedBalance() {
        return formatBalance(balance);
    }

    public static String formatBalance(double amount) {
        if (amount >= 1000000) {
            double million = amount / 1000000.0;
            return String.format("%.1fM", million); 
        } else if (amount >= 1000) {
            double thousand = amount / 1000.0;
            return String.format("%.1fk", thousand);
        } else {
            return String.format("%.0f", amount);  
        }
    }
}

