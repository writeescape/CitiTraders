package me.tehbeard.cititrader.utils;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

public class TraderUtils {

    private TraderUtils(){}
    
    public static boolean isTopInventory(InventoryClickEvent event){
        return (event.getRawSlot() < event.getView().getTopInventory().getSize() && event.getRawSlot() != InventoryView.OUTSIDE);
    }

    public static boolean isBottomInventory(InventoryClickEvent event){

        return (
                event.getRawSlot() >= event.getView().getTopInventory().getSize() &&
                event.getRawSlot() < (event.getView().getTopInventory().getSize()+event.getView().getBottomInventory().getSize()) && 
                event.getRawSlot() != InventoryView.OUTSIDE);
    }

}
