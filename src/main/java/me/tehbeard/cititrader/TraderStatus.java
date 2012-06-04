package me.tehbeard.cititrader;

import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.npc.NPC;

public class TraderStatus {

    public enum Status{
        NOT,
        ITEM_SELECT,
        AMOUNT_SELECT,
        STOCKROOM,
        SET_PRICE
        
    }
    private NPC trader;
    private Status status;
    private double price;
    private Inventory inventory;
    
    public NPC getTrader() {
        return trader;
    }

    public void setTrader(NPC trader) {
        this.trader = trader;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
