package me.tehbeard.cititrader.prompts;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.metadata.Metadatable;

import me.tehbeard.cititrader.StockRoomTrait;
import me.tehbeard.vocalise.parser.ConfigurablePrompt;
import me.tehbeard.vocalise.parser.PromptBuilder;

public class OpenStockRoomPrompt implements ConfigurablePrompt {

    private Prompt next;

    public String getPromptText(ConversationContext context) {
        return null;
    }

    public boolean blocksForInput(ConversationContext context) {
        return false;
    }

    public Prompt acceptInput(ConversationContext context, String input) {
        ((Player)context.getForWhom()).openInventory(((NPC)((Metadatable) context.getForWhom()).getMetadata("npc-editing").get(0).value()).getTrait(StockRoomTrait.class).getInventory());
        return next;
    }

    public void configure(ConfigurationSection section, PromptBuilder builder) {
        builder.makePromptRef(section.getString("id"),this);
        next = section.isString("next") ? builder.locatePromptById(section.getString("next")) : builder.generatePrompt(section.getConfigurationSection("next"));
    }

}
