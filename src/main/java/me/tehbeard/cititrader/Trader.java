package me.tehbeard.cititrader;



import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.metadata.FixedMetadataValue;

import me.tehbeard.cititrader.prompts.ConfirmPurchasePrompt;
import me.tehbeard.cititrader.prompts.GetAmountPrompt;
import me.tehbeard.cititrader.prompts.OpenStockRoomPrompt;
import me.tehbeard.vocalise.parser.PromptBuilder;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
public class Trader extends Character implements Listener{

    PromptBuilder customer = null;
    PromptBuilder owner = null;
    public Trader(){
        //load customer prompts
        customer = new PromptBuilder(CitiTrader.self);
        customer.AddPrompts(GetAmountPrompt.class,ConfirmPurchasePrompt.class);
        customer.load(CitiTrader.self.getResource("tradertalk.yml"));

        //load owner prompts
        owner = new PromptBuilder(CitiTrader.self);
        owner.AddPrompts(OpenStockRoomPrompt.class);
    }

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
            by.setMetadata("npc-editing", new FixedMetadataValue(CitiTrader.self, npc));
            by.openInventory(npc.getTrait(StockRoomTrait.class).getInventory());
        }
        else
        {
            System.out.println("Customer inventory!");
            by.setMetadata("npc-talking-with", new FixedMetadataValue(CitiTrader.self, npc));
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
        if(event.getWhoClicked().hasMetadata("npc-talking-with")){
            event.setCancelled(true);//cancel if talking to an npc
            if(
                    event.getRawSlot() < event.getView().getTopInventory().getSize() && 
                    event.getRawSlot() != InventoryView.OUTSIDE

                    ){
                StockRoomTrait store = ((NPC)event.getWhoClicked().getMetadata("npc-talking-with").get(0).value()).getTrait(StockRoomTrait.class);
                if(store.hasStock(event.getCurrentItem(),false)){


                    event.getWhoClicked().setMetadata("npc-trading-selected", new FixedMetadataValue(CitiTrader.self, event.getCurrentItem()));

                    event.getWhoClicked().closeInventory();
                    customer.makeConversation((Player)event.getWhoClicked());
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
        if(
                !event.getPlayer().hasMetadata("npc-trading-selected") &&
                event.getPlayer().hasMetadata("npc-trading-with")
                ){
            event.getPlayer().removeMetadata("npc-trading-with", CitiTrader.self);
        }
    }

}
