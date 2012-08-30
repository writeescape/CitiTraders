package me.tehbeard.cititrader;

import me.tehbeard.cititrader.WalletTrait.WalletType;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.inventory.Inventory;

public class TraderStatus {

    public enum Status{
        NOT,
        ITEM_SELECT,
        AMOUNT_SELECT,
        STOCKROOM,
        SET_PRICE_SELL,
        SET_PRICE_BUY,
        SET_WALLET,
        SELL_BOX,
        GIVE_MONEY,
        TAKE_MONEY,
        FIRING,
        DISABLE,
        ENABLE
        
        
    }
    private NPC trader;
    private Status status = Status.NOT;
    private double money;
    private Inventory inventory;
    private Inventory tempInv;
    private WalletType walletType;
    private String accName;
    
    
    
    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

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

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public Inventory getInventory() {
        return inventory;
    }
    
    public Inventory getTempInv() {
        return tempInv;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
