package me.tehbeard.cititrader;



import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
public class Trader extends Character implements Listener{

    final Prompt getAmount = new NumericPrompt(){

        public String getPromptText(ConversationContext context) {
            ItemStack is = (ItemStack) ((Metadatable) context.getForWhom()).getMetadata("npc-trading-selected").get(0).value();

            return "Enter the amount of " + is.getData().toString() + "You wish to purchase";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context,
                Number input) {
            if(input.intValue() < 0){return this;}
            if(input.intValue() == 0){return null;}

            StockRoomTrait store = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-trading-with").get(0).value()).getTrait(StockRoomTrait.class);
            ItemStack is = (ItemStack)((Metadatable) context.getForWhom()).getMetadata("npc-trading-selected").get(0).value();

            is.setAmount(input.intValue());
            if(store.hasStock(is, true)){
                //process order
                context.getForWhom().sendRawMessage("Stock available");
                return confirmPurchase;

            }
            else
            {
                context.getForWhom().sendRawMessage("Stock not available");
                return this;
            }


        }

    };

    final Prompt confirmPurchase = new BooleanPrompt(){

        public String getPromptText(ConversationContext context) {
            // TODO Auto-generated method stub
            return "Confirm purchase?";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context,
                boolean confirm) {
            if(confirm){
                StockRoomTrait store = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-trading-with").get(0).value()).getTrait(StockRoomTrait.class);
                ItemStack is = (ItemStack)((Metadatable) context.getForWhom()).getMetadata("npc-trading-selected").get(0).value();

                if(store.hasStock(is, true)){

                    Inventory player = ((Player)context.getForWhom()).getInventory();

                    Inventory chkr = Bukkit.createInventory(null, 9*4);

                    chkr.setContents(player.getContents());
                    if(chkr.addItem(is).size() > 0){
                        context.getForWhom().sendRawMessage("You do not have enough space to purchase that item");
                    }
                    else
                    {
                        //check econ
                        //CitiTrader.economy.bankHas(arg0, arg1)
                        store.getInventory().removeItem(is);
                        player.addItem(is);
                    }
                }
                else
                {
                    context.getForWhom().sendRawMessage("Not enough items to purchase");
                }

            }


            ((Metadatable) context.getForWhom()).removeMetadata("npc-trading-with", CitiTrader.self);
            ((Metadatable) context.getForWhom()).removeMetadata("npc-trading-selected", CitiTrader.self);
            return null;
        }

    };


    @Override
    public void load(DataKey key) throws NPCLoadException {

    }

    @Override
    public void save(DataKey key) {

    }

    @Override
    public void onRightClick(NPC npc, Player by) {
        // TODO Auto-generated method stub
        String owner = npc.getTrait(Owner.class).getOwner();
        if(by.getName().equalsIgnoreCase(owner) && by.getItemInHand().getType() == Material.BOOK){
            System.out.println("Owner inventory!");
            by.openInventory(npc.getTrait(StockRoomTrait.class).getInventory());
        }
        else
        {
            System.out.println("Customer inventory!");
            by.setMetadata("npc-trading-with", new FixedMetadataValue(CitiTrader.self, npc));
            by.openInventory(npc.getTrait(StockRoomTrait.class).constructViewing());
        }
    }

    @Override
    public void onSet(NPC npc) {
        if(!npc.hasTrait(StockRoomTrait.class)){
            npc.addTrait(new StockRoomTrait());
        }
    }


    @EventHandler
    public void inventoryClick(InventoryClickEvent event){
        //is it the top slot
        if(
                event.getRawSlot() < event.getView().getTopInventory().getSize() && 
                event.getRawSlot() != InventoryView.OUTSIDE &&
                event.getWhoClicked().hasMetadata("npc-trading-with")
                ){
            StockRoomTrait store = ((NPC)event.getWhoClicked().getMetadata("npc-trading-with").get(0).value()).getTrait(StockRoomTrait.class);
            if(store.hasStock(event.getCurrentItem(),false)){


                event.getWhoClicked().setMetadata("npc-trading-selected", new FixedMetadataValue(CitiTrader.self, event.getCurrentItem()));

                event.getWhoClicked().closeInventory();
                ((Player)event.getWhoClicked()).beginConversation(new Conversation(CitiTrader.self,(Player)event.getWhoClicked(),getAmount));
            }
        }
    }


    /**
     * 
     * if they close the inventory cancel the trading 
     */
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event){
        if(
                !event.getPlayer().hasMetadata("npc-trading-selected") &&
                event.getPlayer().hasMetadata("npc-trading-with")
                ){
            event.getPlayer().removeMetadata("npc-trading-with", CitiTrader.self);
        }
    }

}
