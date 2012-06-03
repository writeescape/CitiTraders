package me.tehbeard.cititrader.prompts;

import me.tehbeard.cititrader.CitiTrader;
import me.tehbeard.cititrader.StockRoomTrait;
import me.tehbeard.vocalise.parser.ConfigurablePrompt;
import me.tehbeard.vocalise.parser.PromptBuilder;
import me.tehbeard.vocalise.parser.PromptTag;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.Metadatable;

@PromptTag(tag="ConfirmPurchase")
public class ConfirmPurchasePrompt extends BooleanPrompt implements ConfigurablePrompt {

    private Prompt next;

    public String getPromptText(ConversationContext context) {
        // TODO Auto-generated method stub
        return "Confirm purchase?";
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context,
            boolean confirm) {
        if(confirm){
            StockRoomTrait store = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-talking-with").get(0).value()).getTrait(StockRoomTrait.class);
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
                    double cost = is.getAmount() *  store.getPrice(is);
                    String playerName = ((Player)context.getForWhom()).getName();
                    String storeOwner = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-talking-with").get(0).value()).getTrait(Owner.class).getOwner();
                    if(CitiTrader.economy.has(playerName,cost)){
                        if(CitiTrader.economy.withdrawPlayer(playerName, cost).type == ResponseType.SUCCESS){
                            if(CitiTrader.economy.depositPlayer(storeOwner,cost).type==ResponseType.SUCCESS){
                                store.getInventory().removeItem(is);
                                player.addItem(is);
                                context.getForWhom().sendRawMessage("Thank you.");
                            }
                            else
                            {
                                if(CitiTrader.economy.depositPlayer(playerName, cost).type != ResponseType.SUCCESS){
                                    System.out.println("SEVERE ERROR: FAILED TO ROLLBACK TRANSACTION, PLEASE RECREDIT " + playerName + " " + cost);
                                    context.getForWhom().sendRawMessage("An error occured, please notify an operator to refund your account.");
                                }
                            }
                        }
                        else
                        {
                            context.getForWhom().sendRawMessage("Could not transfer funds");
                        }
                    }


                }
            }
            else
            {
                context.getForWhom().sendRawMessage("Not enough items to purchase");
            }

        }


        ((Metadatable) context.getForWhom()).removeMetadata("npc-talking-with", CitiTrader.self);
        ((Metadatable) context.getForWhom()).removeMetadata("npc-trading-selected", CitiTrader.self);
        return next;
    }

    public void configure(ConfigurationSection section, PromptBuilder builder) {
        builder.makePromptRef(section.getString("id"),this);
        next = section.isString("next") ? builder.locatePromptById(section.getString("next")) : builder.generatePrompt(section.getConfigurationSection("next"));

    }

}
