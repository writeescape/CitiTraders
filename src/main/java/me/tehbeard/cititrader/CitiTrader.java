package me.tehbeard.cititrader;

import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Provides a trader for 
 * @author James
 *
 */
public class CitiTrader extends JavaPlugin {

    public static Plugin self;
    public static Economy economy;
    @Override
    public void onEnable() {
        self = this;
        CitizensPlugin citizens = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
        citizens.getCharacterManager().registerCharacter(new CharacterFactory(Trader.class).withName("trader"));
        citizens.getTraitManager().registerTrait(new TraitFactory(StockRoomTrait.class).withName("stockroom").withPlugin(this));
        
        
        Bukkit.getPluginManager().registerEvents((Listener) citizens.getCharacterManager().getCharacter("trader"), this);
        
        
        if(!setupEconomy()){
            getLogger().severe("VAULT NOT FOUND, TRADERS WILL NOT WORK");
        }
        
        getLogger().info("v" + getDescription().getVersion() + " loaded");
    }
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
