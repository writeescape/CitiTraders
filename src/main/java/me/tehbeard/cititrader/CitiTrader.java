package me.tehbeard.cititrader;

import me.tehbeard.cititrader.TraderStatus.Status;
import me.tehbeard.cititrader.WalletTrait.WalletType;
import me.tehbeard.cititrader.utils.ArgumentPack;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Owner;
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
            citizens.getTraitFactory().registerTrait(TraitInfo.create(StockRoomTrait.class).withName("stockroom"));
            citizens.getTraitFactory().registerTrait(TraitInfo.create(WalletTrait.class).withName("wallet"));
            //citizens.getCharacterManager().registerCharacter(new CharacterFactory(Trader.class).withName("trader"));

            Bukkit.getPluginManager().registerEvents(new Trader(), this);




        }else{



            getLogger().severe("COULD NOT FIND AN ECONOMY PLUGIN");
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
        Subcommand subCom;
        try{
        subCom = Subcommand.valueOf(args[0]);
        }
        catch(Exception e){
            return false;
        }
        switch(subCom){
        case create:{
            ArgumentPack argPack = new ArgumentPack(new String[0], new String[] {"type","style"},compact(args,1));
            EntityType npcType = EntityType.PLAYER;
            if(argPack.getOption("type")!=null && isValidNPCType(player,argPack.getOption("type").toUpperCase())){
                npcType = EntityType.fromName(argPack.getOption("type").toUpperCase());
            }

            //TODO: UNCOMMENT WHEN 1.3 COMES OUT
            /*if(argPack.getOption("type")!=null && isValidNPCType(player,argPack.getOption("style").toUpperCase())){
                character = Style.valueOf(argPack.getOption("style").toUpperCase()).getCharacter();
            }*/
            if(argPack.size()!=1){sender.sendMessage(ChatColor.RED+ "Invalid format of arguments");}

            String npcName = argPack.get(0);

            int owned = 0;

            for(NPC npc:citizens.getNPCRegistry()){
                if(npc.hasTrait(StockRoomTrait.class)){
                    if(npc.getTrait(Owner.class).getOwner().equalsIgnoreCase(player.getName())){

                        owned +=1;
                    }
                }
            }
            int traderLimit = getTraderLimit(player);
            if(traderLimit!=-1 && traderLimit <= owned){
                sender.sendMessage(ChatColor.RED + "Cannot spawn another trader NPC!");
                return true;
            }



            //, character);
            NPC npc = citizens.getNPCRegistry().createNPC(npcType, npcName);
            Trader.setUpNPC(npc);
            
            npc.getTrait(Owner.class).setOwner(player.getName());
            npc.spawn(player.getLocation());

            return true;

        }
        case sellprice:{
            if(args.length == 2){
                TraderStatus state = Trader.getStatus(((Player)sender).getName());
                state.setStatus(Status.SET_PRICE_SELL);
                double price = Double.parseDouble(args[1]);
                state.setMoney(price);
            }
            return true;
        }

        case buyprice:{
            if(args.length == 2){
                TraderStatus state = Trader.getStatus(((Player)sender).getName());
                state.setStatus(Status.SET_PRICE_BUY);
                double price = Double.parseDouble(args[1]);
                state.setMoney(price);
            }
            return true;
        }

        case setwallet:{

            if(args.length<2){sender.sendMessage(ChatColor.RED + "Wallet Type needed!");return true;}
            TraderStatus state = Trader.getStatus(((Player)sender).getName());
            WalletType type = WalletType.valueOf(args[1].toUpperCase());
            if(type==null){sender.sendMessage(ChatColor.RED + "Invalid Wallet Type!");return true;}

            if(type == WalletType.BANK && args.length != 3){
                sender.sendMessage(ChatColor.RED + "An account name is needed for this type of wallet");
                return true;
            }
            else{
                String an = "";
                if(args.length>2){
                    an = args[2];
                }
                state.setAccName(an);
            }

            if(!type.hasPermission(sender)){
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this wallet type!");
            }

            state.setStatus(Status.SET_WALLET);
            state.setWalletType(type);


            return true;            
        }

        case wallet:{
            if(args.length<3){sender.sendMessage(ChatColor.RED + "transaction type and amount needed");return true;}

            if(args[1].equalsIgnoreCase("give")){
                TraderStatus status = Trader.getStatus(player.getName());
                status.setStatus(Status.GIVE_MONEY);
                status.setMoney(Double.parseDouble(args[2]));
            }

            if(args[1].equalsIgnoreCase("take")){
                TraderStatus status = Trader.getStatus(player.getName());
                status.setStatus(Status.TAKE_MONEY);
                status.setMoney(Double.parseDouble(args[2]));
            }
            return true;
        }
        case fire:{
            TraderStatus status = Trader.getStatus(player.getName());
            status.setStatus(Status.FIRING);
            return true;
        }
        }
        

        return false;
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
        sellprice,
        buyprice,
        create,
        setwallet,
        wallet,
        fire

    }

    private enum Style{
        trader("trader"),
        villager("villagertrader");

        private String charName;

        private Style(String charName){
            this.charName=charName;
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
