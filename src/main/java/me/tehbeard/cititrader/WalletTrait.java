package me.tehbeard.cititrader;

import org.bukkit.permissions.Permissible;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;

/**
 * Wallet for traders
 * Wallets can be of four types
 * - PRIVATE :: Amount held inside Wallet structure
 * - OWNER   :: Use Owner's wallet
 * - BANK    :: Use a bank account owner owns. (Owner of NPC must be owner of account)
 * - ADMIN   :: Infinite wallet
 * @author James
 *
 */
public class WalletTrait extends Trait {

    public WalletTrait() {
        super("wallet");
    }

    WalletType type = WalletType.PRIVATE;
    double amount = 0;
    String account = "";

    @Override
    public void load(DataKey key) throws NPCLoadException {
        type = WalletType.valueOf(key.getString("type"));
        amount = key.getDouble("amount");
        account = key.getString("account");
    }

    @Override
    public void save(DataKey key) {
        key.setString("type", type.toString());
        key.setDouble("amount",amount);
        key.setString("account",account);
    }

    public enum WalletType{
        PRIVATE,
        OWNER,
        BANK,
        ADMIN;
        
        public boolean hasPermission(Permissible p){
            return p.hasPermission("traders.wallet." + this.toString().toLowerCase());
        }
    }


    /**
     * Add to wallet
     * @param amount
     * @return
     */
    public boolean deposit(double amount){
        if(amount <= 0 ){return false;}
        switch(type){
        case PRIVATE: this.amount+=amount;return true;
        case   OWNER: return CitiTrader.economy.depositPlayer(npc.getTrait(Owner.class).getOwner(), amount).transactionSuccess();
        case    BANK: return CitiTrader.economy.isBankOwner(account, npc.getTrait(Owner.class).getOwner()).transactionSuccess() ? CitiTrader.economy.bankDeposit(account, amount).transactionSuccess() : false;
        case   ADMIN: return true;
        
        }
        
        return false;
    }

    /**
     * remove from wallet
     * @param amount
     * @return
     */
    public boolean withdraw(double amount){
        if(amount <= 0 || amount > this.amount){return false;}
        
        switch(type){
        case PRIVATE: this.amount-=amount;return true;
        case   OWNER: return CitiTrader.economy.withdrawPlayer(npc.getTrait(Owner.class).getOwner(), amount).transactionSuccess();
        case    BANK: return CitiTrader.economy.isBankOwner(account, npc.getTrait(Owner.class).getOwner()).transactionSuccess() ? CitiTrader.economy.bankWithdraw(account, amount).transactionSuccess() : false;
        case   ADMIN: return true;
        }
        return false;
    }

    /**
     * Do they have this much money
     * @param amount
     * @return
     */
    public boolean has(double amount){
        if(amount <= 0 ){return false;}
        switch(type){
        case PRIVATE: return this.amount >= amount;
        case   OWNER: return CitiTrader.economy.has(npc.getTrait(Owner.class).getOwner(), amount);
        case    BANK: return CitiTrader.economy.isBankOwner(account, npc.getTrait(Owner.class).getOwner()).transactionSuccess() ? CitiTrader.economy.bankHas(account, amount).transactionSuccess() : false;
        case   ADMIN: return true;
        }
        return false;
    }

    public final WalletType getType() {
        return type;
    }

    public final void setType(WalletType type) {
        this.type = type;
    }

    public final String getAccount() {
        return account;
    }

    public final void setAccount(String account) {
        this.account = account;
    }

}

