package me.tehbeard.cititrader.prompts;

import me.tehbeard.cititrader.StockRoomTrait;
import me.tehbeard.vocalise.parser.ConfigurablePrompt;
import me.tehbeard.vocalise.parser.PromptBuilder;
import me.tehbeard.vocalise.parser.PromptTag;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.Metadatable;

@PromptTag(tag="GetAmount")
public class GetAmountPrompt extends NumericPrompt implements ConfigurablePrompt{

    
    private Prompt next;

    public String getPromptText(ConversationContext context) {
        ItemStack is = (ItemStack) ((Metadatable) context.getForWhom()).getMetadata("npc-trading-selected").get(0).value();

        StockRoomTrait stockroom = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-talking-with").get(0).value()).getTrait(StockRoomTrait.class);
        
        return "Enter the amount of " + is.getData().toString() + "You wish to purchase (" + stockroom.getPrice(is) + " each)";
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context,
            Number input) {
        if(input.intValue() < 0){return this;}
        if(input.intValue() == 0){return null;}

        StockRoomTrait store = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-talking-with").get(0).value()).getTrait(StockRoomTrait.class);
        ItemStack is = (ItemStack)((Metadatable) context.getForWhom()).getMetadata("npc-trading-selected").get(0).value();

        is.setAmount(input.intValue());
        if(store.hasStock(is, true)){
            //process order
            context.getForWhom().sendRawMessage("Stock available");
            return next;

        }
        else
        {
            context.getForWhom().sendRawMessage("Stock not available");
            return this;
        }


    }

    public void configure(ConfigurationSection section, PromptBuilder builder) {
        builder.makePromptRef(section.getString("id"),this);
        next = section.isString("next") ? builder.locatePromptById(section.getString("next")) : builder.generatePrompt(section.getConfigurationSection("next"));
    }
}
