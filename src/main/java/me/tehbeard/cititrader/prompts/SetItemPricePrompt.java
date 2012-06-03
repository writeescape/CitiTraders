package me.tehbeard.cititrader.prompts;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.Metadatable;

import me.tehbeard.cititrader.StockRoomTrait;
import me.tehbeard.vocalise.parser.ConfigurablePrompt;
import me.tehbeard.vocalise.parser.PromptBuilder;
import me.tehbeard.vocalise.parser.PromptTag;

@PromptTag(tag="SetItemPrice")
public class SetItemPricePrompt extends NumericPrompt implements ConfigurablePrompt {

    Prompt next;

    public String getPromptText(ConversationContext context) {
        return "Please hold the item you wish to set a price for, then tell me how much to sell it for";
    }

    public void configure(ConfigurationSection section, PromptBuilder builder) {
        builder.makePromptRef(section.getString("id"),this);
        next = section.isString("next") ? builder.locatePromptById(section.getString("next")) : builder.generatePrompt(section.getConfigurationSection("next"));

    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context,
            Number input) {
        Player p = (Player) context.getForWhom();
        ItemStack is = p.getItemInHand().clone();
        is.setAmount(1);
        StockRoomTrait stockroom = ((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-editing").get(0).value()).getTrait(StockRoomTrait.class);
        if(input.doubleValue() > 0.0D){
        stockroom.setPrice(is, input.doubleValue());
        }
        return next;
    }



}
