package me.tehbeard.cititrader;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class WalletTrait extends Trait {

    WalletType type;
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
        key.setDouble("amount",key.getDouble("amount"));
        key.setString("account",account);
    }

    public enum WalletType{
        PRIVATE,
        OWNER,
        BANKACCOUNT
    }


    /**
     * Add to wallet
     * @param amount
     * @return
     */
    public boolean deposit(double amount,NPC npc){
        if(amount <= 0 ){return false;}
        switch(type){
        case PRIVATE:this.amount+=amount;break;
        case OWNER: {
            //TODO: CHECKS
            CitiTrader.economy.depositPlayer(npc.getTrait(Owner.class).getOwner(), amount);
        }break;
        case BANKACCOUNT:{
            //TODO: CHECKS
            CitiTrader.economy.bankDeposit(account, amount);
        }break;
        }

        return true;
    }

    /**
     * remove from wallet
     * @param amount
     * @return
     */
    public boolean withdraw(double amount,NPC npc){
        if(amount <= 0 ){return false;}
        switch(type){
        case PRIVATE:this.amount+=amount;break;
        case OWNER: {
            //TODO: CHECKS
            CitiTrader.economy.withdrawPlayer(npc.getTrait(Owner.class).getOwner(), amount);
        }break;
        case BANKACCOUNT:{
            //TODO: CHECKS
            CitiTrader.economy.bankWithdraw(account, amount);
        }break;
        }
        return true;
    }

    /**
     * Do they have this much money
     * @param amount
     * @param npc
     * @return
     */
    public boolean has(double amount,NPC npc){
        if(amount <= 0 ){return false;}
        switch(type){
        case     PRIVATE: return this.amount >= amount;
        case       OWNER: return CitiTrader.economy.has(npc.getTrait(Owner.class).getOwner(), amount);
        case BANKACCOUNT: return CitiTrader.economy.bankHas(account, amount).type == ResponseType.SUCCESS;
        }
        return false;
    }

}

