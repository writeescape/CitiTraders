package me.tehbeard.cititrader;

import me.tehbeard.cititrader.TraderStatus.Status;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        if(setupEconomy()){
        self = this;
        CitizensPlugin citizens = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
        citizens.getTraitManager().registerTrait(new TraitFactory(StockRoomTrait.class).withName("stockroom").withPlugin(this));
        citizens.getCharacterManager().registerCharacter(new CharacterFactory(Trader.class).withName("trader"));
        
        
        
        Bukkit.getPluginManager().registerEvents((Listener) citizens.getCharacterManager().getCharacter("trader"), this);
        
        }else{
            
        
        
            getLogger().severe("VAULT NOT FOUND, TRADERS DISABLED");
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
    
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        // TODO Auto-generated method stub
        
        if(args.length == 2){
            TraderStatus state = Trader.getStatus(((Player)sender).getName());
            state.setStatus(Status.SET_PRICE);
            double price = Double.parseDouble(args[1]);
            state.setPrice(price);
        }
        return true;
    }
}
