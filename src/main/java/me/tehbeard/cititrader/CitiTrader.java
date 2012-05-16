package me.tehbeard.cititrader;

import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Provides a trader for 
 * @author James
 *
 */
public class CitiTrader extends JavaPlugin {

    public static Plugin self;
    @Override
    public void onEnable() {
        self = this;
        CitizensPlugin citizens = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
        citizens.getTraitManager().registerTrait(new TraitFactory(StockRoomTrait.class).withName("stockroom").withPlugin(this));
        citizens.getCharacterManager().registerCharacter(new CharacterFactory(Trader.class).withName("trader"));
        
        
        Bukkit.getPluginManager().registerEvents((Listener) citizens.getCharacterManager().getCharacter("trader"), this);
        
        getLogger().info("v" + getDescription().getVersion() + " loaded");
    }
}
