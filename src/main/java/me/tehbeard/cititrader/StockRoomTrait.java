package me.tehbeard.cititrader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

public class StockRoomTrait extends Trait implements InventoryHolder {

    private Inventory stock;
    Map<ItemStack,Double> prices;
    
    public StockRoomTrait(){
        this(54);
    }

    private StockRoomTrait(int size){
        if(size <= 0 || size > 54){throw new IllegalArgumentException("Size must be between 1 and 54");}

        stock = Bukkit.createInventory(this,size,"stockroom");
        prices = new HashMap<ItemStack, Double>();
    }

    @Override
    public void load(DataKey data) throws NPCLoadException {

        //Load the inventory
        for (DataKey slotKey : data.getRelative("inv").getIntegerSubKeys()){
            stock.setItem(
            Integer.parseInt(slotKey.name()), ItemStorage.loadItemStack(slotKey));
        }
        
        
        for (DataKey priceKey : data.getRelative("prices").getIntegerSubKeys()){
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));
            double price = priceKey.getDouble("price",0);
            prices.put(k, price);
        }
    }

    @Override
    public void save(DataKey data) {
        
        //save the inventory
        int i = 0;
        for(ItemStack is : stock.getContents()){
            if(is !=null){
                
                DataKey inv = data.getRelative("inv");
            ItemStorage.saveItem(inv.getRelative("" + i++),is);
            }
        }

        DataKey priceIndex = data.getRelative("prices");
        i = 0;
        for(Entry<ItemStack,Double> price : prices.entrySet()){
            ItemStorage.saveItem(priceIndex.getRelative("" + i).getRelative("item"), price.getKey());
            priceIndex.getRelative("" + i).setDouble("price", price.getValue());
        }
    }

    public Inventory getInventory() {
        return stock;
    }
    
    
    /**
     * Contstruct a viewing inventory 
     * @return
     */
    public Inventory constructViewing(){
        
        
        Inventory display = Bukkit.createInventory(null, 54,"Store");
        
        for(ItemStack is : stock){
            if(is == null){continue;}
            ItemStack chk = new ItemStack(is.getType(),1,is.getDurability());
            chk.addEnchantments(is.getEnchantments());
            if(display.contains(chk) == false && getPrice(is) > 0.0D){
                display.addItem(chk);
            }
            
        }
        
        return display;
        
      
    }
    
    /**
     * Does this stockroom contain this item
     * @param locate Item to look for
     * @param checkAmount
     * @return
     */
    public boolean hasStock(ItemStack locate,boolean checkAmount){
        Material material = locate.getType();
        int amount = locate.getAmount();
        
        int amountFound = 0;
        if(stock.contains(material)){
            for( Entry<Integer, ? extends ItemStack> e : stock.all(material).entrySet()){
                ItemStack i  = e.getValue();
                if(i.equals(locate)){
                    amountFound += i.getAmount();
                }
                
            }
            
            return checkAmount ? amount <= amountFound : amountFound > 0;
        }
        return false;
    }

    public double getPrice(ItemStack is){
        ItemStack i = is.clone();
        i.setAmount(1);
        return prices.containsKey(i) ? prices.get(i) : 0;
        
    }
    
    public void setPrice(ItemStack is,double price){
        ItemStack i = is.clone();
        i.setAmount(1);
        prices.put(i, price);
        
    }
}
