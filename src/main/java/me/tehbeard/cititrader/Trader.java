package me.tehbeard.cititrader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import me.tehbeard.cititrader.TraderStatus.Status;
import me.tehbeard.cititrader.WalletTrait.WalletType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

/**
 * @author James
 *
 */
public class Trader implements Listener {

    private static Map<String, TraderStatus> status;

    public static TraderStatus getStatus(String player) {
        if (!status.containsKey(player)) {
            status.put(player, new TraderStatus());
        }
        return status.get(player);

    }

    public static void clearStatus(String player) {
        status.remove(player);

    }

    public Trader() {
        status = new HashMap<String, TraderStatus>();
    }

    @EventHandler
    public void onCitizensLoad(CitizensEnableEvent event) {
        try {
            Metrics metrics = new Metrics(CitiTrader.self);
            Graph graph = metrics.createGraph("Traders");
            graph.addPlotter(new Metrics.Plotter("Total Traders") {
                @Override
                public int getValue() {
                    
                    Integer totaltrader = 0;
                    try {
                        Iterator it = CitizensAPI.getNPCRegistry().iterator();
                    while (it.hasNext()) {
                        NPC npcount = (NPC) it.next();
                        if (npcount.hasTrait(StockRoomTrait.class)) {
                            totaltrader++;
                        }
                    }
                    } catch (Exception e) {
                        System.out.println("error");
                        e.printStackTrace();
                    }
                    if(CitiTrader.self.getConfig().getBoolean("debug.tradercount", false)) {
                        CitiTrader.self.getLogger().info("Traders: " + totaltrader);
                    }
                    return totaltrader;
                }
            });
            metrics.start();
            CitiTrader.self.getLogger().info("Metrics Started.");
        } catch (IOException e) {
            CitiTrader.self.getLogger().info("Failed:");
            e.printStackTrace();
        }
        
        if(CitiTrader.self.getConfig().getBoolean("debug.versioncheck", true)) {
            CitiTrader.self.checkVersion();
        }
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent event) {
        NPC npc = event.getNPC();
        Player by = event.getClicker();

        if (!npc.hasTrait(StockRoomTrait.class)) {
            return;
        }
        if (!npc.getTrait(StockRoomTrait.class).getDisabled()) {
            npc.getTrait(StockRoomTrait.class).openBuyWindow(by);
        } else {
            by.sendMessage(ChatColor.DARK_PURPLE + "This trader is currently disabled.");
        }

    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player by = event.getClicker();
        if (!npc.hasTrait(StockRoomTrait.class)) {
            return;
        }

        if (!npc.getTrait(StockRoomTrait.class).isEnableRightClick()) {
            return;
        }
        TraderStatus state = getStatus(by.getName());
        state.setTrader(npc);
        String owner = npc.getTrait(Owner.class).getOwner();

        if (by.getName().equalsIgnoreCase(owner) || by.getName().equalsIgnoreCase(npc.getName())) {
            switch (state.getStatus()) {
                case DISABLE:
                    state.getTrader().getTrait(StockRoomTrait.class).setDisabled(true);
                    clearStatus(by.getName());
                    by.sendMessage(ChatColor.DARK_PURPLE + "Trader " + npc.getName() + " has been disabled.");
                    return;
                case ENABLE:
                    state.getTrader().getTrait(StockRoomTrait.class).setDisabled(false);
                    clearStatus(by.getName());
                    by.sendMessage(ChatColor.DARK_PURPLE + "Trader " + npc.getName() + " has been enabled.");
                    return;
            }
        }
        if (by.getName().equalsIgnoreCase(owner)) {

            switch (state.getStatus()) {
                case FIRING: {
                    if (!state.getTrader().getTrait(StockRoomTrait.class).isStockRoomEmpty()) {
                        by.sendMessage(ChatColor.RED + "Cannot fire trader! He still has items on him!");
                        clearStatus(by.getName());
                        return;
                    }
                    if (state.getTrader().getTrait(WalletTrait.class).getType() == WalletType.PRIVATE && state.getTrader().getTrait(WalletTrait.class).getAmount() > 0.0D) {
                        by.sendMessage(ChatColor.RED + "Trader still has money in their wallet!");
                        clearStatus(by.getName());
                        return;
                    }

                    by.sendMessage(ChatColor.DARK_RED + "Firing trader!");
                    npc.removeTrait(StockRoomTrait.class);
                    npc.removeTrait(WalletTrait.class);
                    npc.destroy();
                }
                case SET_PRICE_SELL: {
                    state.getTrader().getTrait(StockRoomTrait.class).setSellPrice(by.getItemInHand(), state.getMoney());
                    state.setStatus(Status.NOT);
                    if(state.getMoney() == -1) {
                        by.sendMessage(ChatColor.GREEN + "Item price removed.");
                    } else {
                        by.sendMessage(ChatColor.GREEN + "Sell price set.");
                    }
                    return;
                }

                case SET_PRICE_BUY: {
                    state.getTrader().getTrait(StockRoomTrait.class).setBuyPrice(by.getItemInHand(), state.getMoney());
                    state.setStatus(Status.NOT);
                    if(state.getMoney() == -1) {
                        by.sendMessage(ChatColor.GREEN + "Item price removed.");
                    } else {
                        by.sendMessage(ChatColor.GREEN + "Buy price set.");
                    }
                    return;
                }

                case SET_WALLET: {
                    state.getTrader().getTrait(WalletTrait.class).setAccount(state.getAccName());
                    state.getTrader().getTrait(WalletTrait.class).setType(state.getWalletType());
                    state.setStatus(Status.NOT);
                    by.sendMessage(ChatColor.GREEN + "Wallet information set");
                    return;
                }

                case GIVE_MONEY: {
                    if (state.getTrader().getTrait(WalletTrait.class).getType() != WalletType.PRIVATE) {
                        by.sendMessage(ChatColor.RED + "Cannot use give/take on traders who use economy backed accounts.");
                        return;
                    }
                    if (!CitiTrader.economy.has(by.getName(), state.getMoney())) {
                        by.sendMessage(ChatColor.RED + "Not enough funds.");
                    }
                    if (!state.getTrader().getTrait(WalletTrait.class).deposit(state.getMoney())) {
                        by.sendMessage(ChatColor.RED + "Cannot  give trader the money.");
                        return;
                    }
                    if (!CitiTrader.economy.withdrawPlayer(by.getName(), state.getMoney()).transactionSuccess()) {
                        by.sendMessage(ChatColor.RED + "Cannot give trader the money from your wallet.");
                        state.getTrader().getTrait(WalletTrait.class).withdraw(state.getMoney());
                        return;
                    }
                    by.sendMessage(ChatColor.GREEN + "Money given");
                    status.remove(by.getName());
                    return;
                }
                case TAKE_MONEY: {

                    if (state.getTrader().getTrait(WalletTrait.class).getType() != WalletType.PRIVATE) {
                        by.sendMessage(ChatColor.RED + "Cannot use give/take on traders who use economy backed accounts.");
                        return;
                    }
                    WalletTrait wallet = state.getTrader().getTrait(WalletTrait.class);

                    if (!wallet.has(state.getMoney())) {
                        by.sendMessage(ChatColor.RED + "Not enough funds.");
                        return;
                    }
                    if (!CitiTrader.economy.depositPlayer(by.getName(), state.getMoney()).transactionSuccess()) {
                        by.sendMessage(ChatColor.RED + "Cannot take the money.");
                        return;
                    }
                    if (!wallet.withdraw(state.getMoney())) {
                        by.sendMessage(ChatColor.RED + "Cannot take the money from the trader's wallet.");
                        CitiTrader.economy.withdrawPlayer(by.getName(), state.getMoney());
                        return;
                    }
                    by.sendMessage(ChatColor.GREEN + "Money given");
                    status.remove(by.getName());
                    return;
                }
                case SET_LINK: {
                    if(!state.getTrader().getTrait(StockRoomTrait.class).setLinkedNPC(state.getLinkedNPCName())) {
                        by.sendMessage("Trader could not be linked to " + state.getLinkedNPCName());
                        state.setStatus(Status.NOT);
                        return;
                    }
                    
                    by.sendMessage("Trader linked to " + state.getLinkedNPCName());
                    state.setStatus(Status.NOT);
                    return;
                }
                case REMOVE_LINK:
                    if(!state.getTrader().getTrait(StockRoomTrait.class).removeLinkedNPC()) {
                        by.sendMessage("Trader could not be unlinked.");
                        state.setStatus(Status.NOT);
                        return;
                    }
                    by.sendMessage("Trader has been unlinked, he will use is own pricelist now.");
                    state.setStatus(Status.NOT);
                    return;
            }

        }


        if (by.getName().equalsIgnoreCase(owner) && by.getItemInHand().getType() == Material.BOOK) {
            npc.getTrait(StockRoomTrait.class).openStockRoom(by);

        } else {
            if (!npc.getTrait(StockRoomTrait.class).getDisabled()) {
                npc.getTrait(StockRoomTrait.class).openSalesWindow(by);
            } else {
                by.sendMessage(ChatColor.DARK_PURPLE + "This shop is currently disabled.");
            }
        }
    }

    public static void setUpNPC(NPC npc) {
        if (!npc.hasTrait(StockRoomTrait.class)) {
            npc.addTrait(StockRoomTrait.class);

        }

        if (!npc.hasTrait(WalletTrait.class)) {
            npc.addTrait(WalletTrait.class);

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventoryClick(InventoryClickEvent event) {
        TraderStatus state = getStatus(event.getWhoClicked().getName());

        if (state.getStatus() != Status.NOT) {
            state.getTrader().getTrait(StockRoomTrait.class).processInventoryClick(event);
        }
    }

    /**
     *
     * if they close the inventory cancel the trading
     */
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        TraderStatus state = getStatus(event.getPlayer().getName());
        if (state.getStatus() != Status.NOT) {
            state.getTrader().getTrait(StockRoomTrait.class).processInventoryClose(event);
        }
    }
    
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        if(event.getPlayer().isOp() && CitiTrader.outdated) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Your version of Cititraders(" + CitiTrader.self.getDescription().getVersion() + ") is outdated, please update.");
        }
    }
}
