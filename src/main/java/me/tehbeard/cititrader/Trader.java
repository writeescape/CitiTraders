package me.tehbeard.cititrader;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.tehbeard.cititrader.TraderStatus.Status;
import me.tehbeard.cititrader.WalletTrait.WalletType;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;


/**
 * @author James
 *
 */
public class Trader extends Character implements Listener{



    private static Map<String,TraderStatus> status;

    public static TraderStatus getStatus(String player){
        if(!status.containsKey(player)){status.put(player,new TraderStatus());}
        return status.get(player);

    }


    public Trader(){
        status = new HashMap<String, TraderStatus>();
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {

    }

    @Override
    public void save(DataKey key) {

    }

    @Override
    public void onLeftClick(NPC npc, Player by) {

        TraderStatus state = getStatus(by.getName());
        state.setTrader(npc);
        String owner = npc.getTrait(Owner.class).getOwner();

        if(!by.getName().equalsIgnoreCase(owner) && state.getStatus() == Status.NOT){

            state.setStatus(Status.SELL_BOX);
            Inventory i = npc.getTrait(StockRoomTrait.class).constructSellBox();
            state.setInventory(i);
            by.openInventory(i);
            return;
        }

    }


    public void onRightClick(NPC npc, Player by) {


        TraderStatus state = getStatus(by.getName());
        state.setTrader(npc);
        String owner = npc.getTrait(Owner.class).getOwner();

        if(by.getName().equalsIgnoreCase(owner)){

            switch(state.getStatus()){
            case SET_PRICE_SELL:{
                state.getTrader().getTrait(StockRoomTrait.class).setSellPrice(by.getItemInHand(),state.getMoney());
                state.setStatus(Status.NOT);
                by.sendMessage("Price set");
                return;
            }
            
            case SET_PRICE_BUY:{
                state.getTrader().getTrait(StockRoomTrait.class).setBuyPrice(by.getItemInHand(),state.getMoney());
                state.setStatus(Status.NOT);
                by.sendMessage("Price set");
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
            System.out.println("Owner inventory!");
            state.setStatus(Status.STOCKROOM);
            by.openInventory(state.getTrader().getTrait(StockRoomTrait.class).getInventory());
        }else if(by.getName().equalsIgnoreCase(owner) && state.getStatus() == Status.SET_PRICE_SELL){
            //SET PRICE
        }
        else
        {
            System.out.println("Customer inventory!");
            state.setStatus(Status.ITEM_SELECT);
            state.setInventory(state.getTrader().getTrait(StockRoomTrait.class).constructViewing());
            by.openInventory(state.getInventory());


        }
    }

    @Override
    public void onSet(NPC npc) {
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

        //cancel if not item or not in trade windows
        if(event.getCurrentItem().getType() == Material.AIR || state.getStatus() == Status.NOT){return;}

        //cancel the event.
        event.setCancelled(true);



        switch(state.getStatus()){

        //selecting item to purchase
        case ITEM_SELECT:{
            if(!isTopInventory(event)){break;}
            if(event.isShiftClick()){
                ItemStack is = event.getCurrentItem().clone();
                //clear the inventory
                for(int i =0;i<54; i++){
                    state.getInventory().setItem(i, null);
                }

                //set up the amount selection
                int k = 0;
                for(int i=64;i>0;i/=2){
                    is.setAmount(i);
                    state.getInventory().setItem(k, is);
                    k++;
                }
                state.setStatus(Status.AMOUNT_SELECT);
                System.out.println("ITEM SELECTED");
            }
            else
            {
                Player p = (Player) event.getWhoClicked();
                double price = state.getTrader().getTrait(StockRoomTrait.class).getSellPrice(event.getCurrentItem());
                p.sendMessage("Item costs: " + price);
            }
        }break;

        //Amount selection window
        case AMOUNT_SELECT:{
            if(!isTopInventory(event)){break;}
            if(event.isShiftClick()){
                System.out.println("AMOUNT SELECTED");
                Player player = (Player) event.getWhoClicked();
                sellToPlayer(player,state.getTrader(),event.getCurrentItem());

            }
            else
            {
                Player p = (Player) event.getWhoClicked();
                double price = state.getTrader().getTrait(StockRoomTrait.class).getSellPrice(event.getCurrentItem()) * event.getCurrentItem().getAmount();
                p.sendMessage("Stack costs: " + price);
            }
        }break;

        case SELL_BOX:{
            if(!isTopInventory(event) && !isBottomInventory(event)){break;}

            if(event.isShiftClick()){
                Player p = (Player) event.getWhoClicked();
                double price = state.getTrader().getTrait(StockRoomTrait.class).getBuyPrice(event.getCurrentItem());
                p.sendMessage("item price: " + price);
                p.sendMessage("stack price:" + (price * event.getCurrentItem().getAmount()));
            }
        }

        }



    }


    public boolean isTopInventory(InventoryClickEvent event){
        return (event.getRawSlot() < event.getView().getTopInventory().getSize() && event.getRawSlot() != InventoryView.OUTSIDE);
    }

    public boolean isBottomInventory(InventoryClickEvent event){

        return (
                event.getRawSlot() >= event.getView().getTopInventory().getSize() &&
                event.getRawSlot() < (event.getView().getTopInventory().getSize()+event.getView().getBottomInventory().getSize()) && 
                event.getRawSlot() != InventoryView.OUTSIDE);
    }


    /**
     * 
     * if they close the inventory cancel the trading 
     */
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event){
        TraderStatus state = getStatus(event.getPlayer().getName());

        if(state.getStatus() == Status.SELL_BOX){
            Iterator<ItemStack> it = state.getInventory().iterator();
            while(it.hasNext()){
                ItemStack is = it.next();
                if(is==null){continue;}

                double price = state.getTrader().getTrait(StockRoomTrait.class).getBuyPrice(is);

                //check we buy it.
                if(price == 0.0D){continue;}




                //check space
                Inventory chkr = Bukkit.createInventory(null, 9*4);
                chkr.setContents(state.getTrader().getTrait(StockRoomTrait.class).getInventory().getContents());
                if(chkr.addItem(is).size() > 0){
                    ((CommandSender) event.getPlayer()).sendMessage(ChatColor.RED + "Trader does not have enough space to hold something you sold him.");
                    continue;
                }
                chkr = null;

                WalletTrait wallet = state.getTrader().getTrait(WalletTrait.class);
                //check we have the cash
                double sale = price * is.getAmount();
                if(!wallet.has(sale)){
                    ((CommandSender) event.getPlayer()).sendMessage(ChatColor.RED + "Trader does not have the funds to pay you.");
                    break;
                }

                //give cash
                if(!wallet.withdraw(sale)){
                    ((CommandSender) event.getPlayer()).sendMessage(ChatColor.RED + "Trader couldn't find their wallet.");
                    break;
                }

                if(!CitiTrader.economy.depositPlayer(event.getPlayer().getName(), sale).transactionSuccess()){
                    ((CommandSender) event.getPlayer()).sendMessage(ChatColor.RED + "Couldn't find your wallet.");
                    wallet.deposit(sale);
                    break;
                }

                //take item
                it.remove();
                state.getTrader().getTrait(StockRoomTrait.class).getInventory().addItem(is);


            }

            //drop all items in sellbox inventory
            it = state.getInventory().iterator();
            while(it.hasNext()){
                ItemStack is = it.next();
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(),is);
            }

        }

        status.remove(event.getPlayer().getName());

    }

    private void sellToPlayer(Player player,NPC npc,ItemStack is){
        //TODO: If admin shop, do not deduct items.
        StockRoomTrait store = npc.getTrait(StockRoomTrait.class);


        if(store.hasStock(is, true)){

            Inventory playerInv = player.getInventory();

            Inventory chkr = Bukkit.createInventory(null, 9*4);

            chkr.setContents(playerInv.getContents());
            if(chkr.addItem(is).size() > 0){
                player.sendMessage(ChatColor.RED + "You do not have enough space to purchase that item");
            }
            else
            {
                //check econ
                WalletTrait wallet = npc.getTrait(WalletTrait.class);
                double cost = is.getAmount() *  store.getSellPrice(is);
                String playerName = player.getName();
                if(CitiTrader.economy.has(playerName,cost)){
                    if(CitiTrader.economy.withdrawPlayer(playerName, cost).type == ResponseType.SUCCESS){

                        //if(CitiTrader.economy.depositPlayer(storeOwner,cost).type==ResponseType.SUCCESS){
                        if(wallet.deposit(cost)){
                            store.getInventory().removeItem(is);
                            playerInv.addItem(is);                           
                        }
                        else
                        {
                            if(CitiTrader.economy.depositPlayer(playerName, cost).type != ResponseType.SUCCESS){
                                System.out.println("SEVERE ERROR: FAILED TO ROLLBACK TRANSACTION, PLEASE RECREDIT " + playerName + " " + cost);
                                player.sendMessage(ChatColor.RED + "An error occured, please notify an operator to refund your account.");
                            }
                        }
                    }
                    else
                    {
                        player.sendMessage(ChatColor.RED + "Could not transfer funds");
                    }
                }
                else
                {
                    player.sendMessage(ChatColor.RED + "You do not have enough money!");
                }


            }
        }
        else
        {
            player.sendMessage(ChatColor.RED + "Not enough items to purchase");
        }
    }

}
