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
        return Messages.formatBalance(balance);
    }
}

