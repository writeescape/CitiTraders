package me.tehbeard.cititrader;

import me.tehbeard.cititrader.TraderStatus.Status;
import me.tehbeard.cititrader.utils.ArgumentPack;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.npc.character.Character;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
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

    public static final String PERM_PREFIX = "traders";
    public static Plugin self;
    public static Economy economy;
    private static CitizensPlugin citizens;
    @Override
    public void onEnable() {
        if(!getConfig().contains("profiles")){
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        if(setupEconomy()){
            self = this;
            citizens = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
            citizens.getTraitManager().registerTrait(new TraitFactory(StockRoomTrait.class).withName("stockroom").withPlugin(this));
            citizens.getTraitManager().registerTrait(new TraitFactory(WalletTrait.class).withName("wallet").withPlugin(this));
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

        if(args.length==0){return false;}
        if(sender instanceof Player == false){sender.sendMessage("DOES NOT WORK FROM CONSOLE");return true;}

        Player player = (Player)sender;
        if(!sender.hasPermission(PERM_PREFIX + ".command." + args[0])){return false;}
        switch(Subcommand.valueOf(args[0])){
        case create:{
            ArgumentPack argPack = new ArgumentPack(new String[0], new String[] {"type","style"},compact(args,1));
            EntityType npcType = EntityType.PLAYER;
            if(argPack.getOption("type")!=null && isValidNPCType(player,argPack.getOption("type").toUpperCase())){
                npcType = EntityType.fromName(argPack.getOption("type").toUpperCase());
            }

            Character character = Style.trader.getCharacter();
            //TODO: UNCOMMENT WHEN 1.3 COMES OUT
            /*if(argPack.getOption("type")!=null && isValidNPCType(player,argPack.getOption("style").toUpperCase())){
                character = Style.valueOf(argPack.getOption("style").toUpperCase()).getCharacter();
            }*/
            if(argPack.size()!=1){sender.sendMessage(ChatColor.RED+ "Invalid format of arguments");}

            String npcName = argPack.get(0);

            int owned = 0;
            for(NPC npc:citizens.getNPCRegistry().getNPCs(Trader.class)){
                if(npc.getTrait(Owner.class).getOwner().equalsIgnoreCase(player.getName())){

                    owned +=1;
                }
            }
            int traderLimit = getTraderLimit(player);
            if(traderLimit!=-1 && traderLimit <= owned){
                sender.sendMessage(ChatColor.RED + "Cannot spawn another trader NPC!");
                return true;
            }



            NPC npc = citizens.getNPCRegistry().createNPC(npcType, npcName, character);
            npc.getTrait(Owner.class).setOwner(player.getName());
            npc.spawn(player.getLocation());

            return true;

        }
        case setprice:{
            if(args.length == 2){
                TraderStatus state = Trader.getStatus(((Player)sender).getName());
                state.setStatus(Status.SET_PRICE);
                double price = Double.parseDouble(args[1]);
                state.setPrice(price);
            }
        }

        }

        return true;
    }

    private String compact(String[] a ,int idx){
        String s = "";
        for(int i = idx;i<a.length;i++){
            if(s.length() > 0){s+= " ";}
            s+=a[i];
        }
        return s;
    }

    private enum Subcommand{
        setprice,
        create,
        setwallet,

    }

    private enum Style{
        trader("trader"),
        villager("villagertrader");

        private String charName;

        private Style(String charName){
            this.charName=charName;
        }

        public Character getCharacter(){
            return citizens.getCharacterManager().getCharacter(charName);
        }


    }

    public boolean isValidNPCType(Player player,String type){
        return getConfig().getStringList("trader-types").contains(type);
    }

    public boolean isValidTraderStyle(Player player){
        return true;//TODO: Proper checks when 1.3 hits
    }

    public int getTraderLimit(Player player){
        int limit =  getConfig().getInt("profiles.default.trader-limit"); 
        for(String s : getConfig().getConfigurationSection("profiles").getKeys(false)){
            if(s.equals("default")){continue;}
            if(player.hasPermission(PERM_PREFIX + ".profile." + s)){
                limit = Math.max(getConfig().getInt("profiles." + s + ".trader-limit"),limit);
            }

        }

        return limit;
    }
}
