package me.tehbeard.cititrader;



import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.tehbeard.cititrader.TraderStatus.Status;
import me.tehbeard.cititrader.WalletTrait.WalletType;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;



/**
 * @author James
 *
 */
public class Trader implements Listener{



    private static Map<String,TraderStatus> status;

    public static TraderStatus getStatus(String player){
        if(!status.containsKey(player)){status.put(player,new TraderStatus());}
        return status.get(player);

    }

    public static void clearStatus(String player){
        status.remove(player);

    }


    public Trader(){
        status = new HashMap<String, TraderStatus>();
    }



    @EventHandler
    public void onLeftClick(NPCLeftClickEvent  event) {
        NPC npc = event.getNPC();
        Player by = event.getClicker();

        if(!npc.hasTrait(StockRoomTrait.class)){
            return;
        }
        if(npc.getTrait(StockRoomTrait.class).isEnableLeftClick()){
            npc.getTrait(StockRoomTrait.class).openBuyWindow(by);
        }

    }


    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player by = event.getClicker();
        if(!npc.hasTrait(StockRoomTrait.class)){
            return;
        }

        if(!npc.getTrait(StockRoomTrait.class).isEnableRightClick()){
            return;
        }
        TraderStatus state = getStatus(by.getName());
        state.setTrader(npc);
        String owner = npc.getTrait(Owner.class).getOwner();

        if(by.getName().equalsIgnoreCase(owner)){

            switch(state.getStatus()){
            case FIRING:{
                if(!state.getTrader().getTrait(StockRoomTrait.class).isStockRoomEmpty()){
                    by.sendMessage("Cannot fire trader! He still has items on him!");
                    clearStatus(by.getName());
                    return;
                }
                if(state.getTrader().getTrait(WalletTrait.class).getType() == WalletType.PRIVATE && state.getTrader().getTrait(WalletTrait.class).getAmount() > 0.0D){
                    by.sendMessage("Trader still has money in their wallet!");
                    clearStatus(by.getName());
                    return;
                }

                by.sendMessage("Firing trader!");
                npc.removeTrait(StockRoomTrait.class);
                npc.removeTrait(WalletTrait.class);
                npc.destroy();
            }
            case SET_PRICE_SELL:{
                state.getTrader().getTrait(StockRoomTrait.class).setSellPrice(by.getItemInHand(),state.getMoney());
                state.setStatus(Status.NOT);
                by.sendMessage("Sell Price set");
                return;
            }

            case SET_PRICE_BUY:{
                state.getTrader().getTrait(StockRoomTrait.class).setBuyPrice(by.getItemInHand(),state.getMoney());
                state.setStatus(Status.NOT);
                by.sendMessage("Buy Price set");
                return;
            }

            case SET_WALLET:
            {
                state.getTrader().getTrait(WalletTrait.class).setAccount(state.getAccName());
                state.getTrader().getTrait(WalletTrait.class).setType(state.getWalletType());
                state.setStatus(Status.NOT);
                by.sendMessage("Wallet information set");
                return;
            }

            case GIVE_MONEY:{
                if(state.getTrader().getTrait(WalletTrait.class).getType() != WalletType.PRIVATE){
                    by.sendMessage(ChatColor.RED + "Cannot use give/take on traders who use economy backed accounts.");
                    return;
                }
                if(!CitiTrader.economy.has(by.getName(), state.getMoney())){
                    by.sendMessage(ChatColor.RED + "Not enough funds.");
                }
                if(!state.getTrader().getTrait(WalletTrait.class).deposit(state.getMoney())){
                    by.sendMessage(ChatColor.RED + "Cannot  give trader the money.");
                    return;
                }
                if(!CitiTrader.economy.withdrawPlayer(by.getName(), state.getMoney()).transactionSuccess()){
                    by.sendMessage(ChatColor.RED + "Cannot give trader the money from your wallet.");
                    state.getTrader().getTrait(WalletTrait.class).withdraw(state.getMoney());
                    return;
                }
                by.sendMessage(ChatColor.GREEN + "Money given");
                status.remove(by.getName());
                return;
            }
            case TAKE_MONEY:

                if(state.getTrader().getTrait(WalletTrait.class).getType() != WalletType.PRIVATE){
                    by.sendMessage(ChatColor.RED + "Cannot use give/take on traders who use economy backed accounts.");
                    return;
                }
                WalletTrait wallet = state.getTrader().getTrait(WalletTrait.class);

                if(!wallet.has(state.getMoney())){
                    by.sendMessage(ChatColor.RED + "Not enough funds.");
                    return;
                }
                if(!CitiTrader.economy.depositPlayer(by.getName(),state.getMoney()).transactionSuccess()){
                    by.sendMessage(ChatColor.RED + "Cannot take the money.");
                    return;
                }
                if(!wallet.withdraw(state.getMoney())){
                    by.sendMessage(ChatColor.RED + "Cannot take the money from the trader's wallet.");
                    CitiTrader.economy.withdrawPlayer(by.getName(), state.getMoney());
                    return;
                }
                by.sendMessage(ChatColor.GREEN + "Money given");
                status.remove(by.getName());
                return;
            }

        }


        if(by.getName().equalsIgnoreCase(owner) && by.getItemInHand().getType() == Material.BOOK){
            npc.getTrait(StockRoomTrait.class).openStockRoom(by);

        }
        else{
            npc.getTrait(StockRoomTrait.class).openSalesWindow(by);
        }
    }

    public static void setUpNPC(NPC npc) {
        if(!npc.hasTrait(StockRoomTrait.class)){
            npc.addTrait(StockRoomTrait.class);

        }

        if(!npc.hasTrait(WalletTrait.class)){
            npc.addTrait(WalletTrait.class);

        }
    }


    @EventHandler
    public void inventoryClick(InventoryClickEvent event){
        TraderStatus state = getStatus(event.getWhoClicked().getName());
        if(state.getStatus() != Status.NOT){
            state.getTrader().getTrait(StockRoomTrait.class).processInventoryClick(event);
        }
    }




    /**
     * 
     * if they close the inventory cancel the trading 
     */
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event){
        TraderStatus state = getStatus(event.getPlayer().getName());
        if(state.getStatus() != Status.NOT){
            state.getTrader().getTrait(StockRoomTrait.class).processInventoryClose(event);
        }
    }



}
