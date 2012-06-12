package me.tehbeard.cititrader;



import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.tehbeard.cititrader.TraderStatus.Status;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;


/**
 * TODO: Add Wallets
 * TODO: Add Buying capability
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
    public void onRightClick(NPC npc, Player by) {

        
        TraderStatus state = getStatus(by.getName());
        state.setTrader(npc);
        String owner = npc.getTrait(Owner.class).getOwner();

        if(by.getName().equalsIgnoreCase(owner) && state.getStatus() == Status.SET_PRICE){
            state.getTrader().getTrait(StockRoomTrait.class).setPrice(by.getItemInHand(),state.getPrice());
            state.setStatus(Status.NOT);
            by.sendMessage("Price set");
            return;
        }


        if(by.getName().equalsIgnoreCase(owner) && by.getItemInHand().getType() == Material.BOOK){
            System.out.println("Owner inventory!");
            state.setStatus(Status.STOCKROOM);
            by.openInventory(state.getTrader().getTrait(StockRoomTrait.class).getInventory());
        }else if(by.getName().equalsIgnoreCase(owner) && state.getStatus() == Status.SET_PRICE){
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
        if(status.containsKey(event.getWhoClicked().getName()) &&status.get(event.getWhoClicked().getName()).getStatus() != Status.STOCKROOM){
            event.setCancelled(true);
            if( event.getRawSlot() < event.getView().getTopInventory().getSize() && event.getRawSlot() != InventoryView.OUTSIDE){
                TraderStatus state = getStatus(event.getWhoClicked().getName());
                
                
                if(event.getCurrentItem().getType() == Material.AIR){return;}
                
                
                switch(state.getStatus()){
                case ITEM_SELECT:{
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
                        double price = state.getTrader().getTrait(StockRoomTrait.class).getPrice(event.getCurrentItem());
                        p.sendMessage("Item costs: " + price);
                    }
                }break;
                case AMOUNT_SELECT:{
                    if(event.isShiftClick()){
                        System.out.println("AMOUNT SELECTED");
                        Player player = (Player) event.getWhoClicked();
                        trade(player,state.getTrader(),event.getCurrentItem());
                        
                    }
                    else
                    {
                        Player p = (Player) event.getWhoClicked();
                        double price = state.getTrader().getTrait(StockRoomTrait.class).getPrice(event.getCurrentItem()) * event.getCurrentItem().getAmount();
                        p.sendMessage("Stack costs: " + price);
                    }
                }break;

                }
            }
        }

    }


    /**
     * 
     * if they close the inventory cancel the trading 
     */
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event){
        if(status.containsKey(event.getPlayer().getName())){
            status.remove(event.getPlayer().getName());
        }
    }
    
    private void trade(Player player,NPC npc,ItemStack is){
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
                double cost = is.getAmount() *  store.getPrice(is);
                String playerName = player.getName();
                String storeOwner = npc.getTrait(Owner.class).getOwner();
                if(CitiTrader.economy.has(playerName,cost)){
                    if(CitiTrader.economy.withdrawPlayer(playerName, cost).type == ResponseType.SUCCESS){
                        if(CitiTrader.economy.depositPlayer(storeOwner,cost).type==ResponseType.SUCCESS){
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
