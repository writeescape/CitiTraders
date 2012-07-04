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
    Map<ItemStack,Double> sellPrices;
    Map<ItemStack,Double> buyPrices;
    public StockRoomTrait(){
        this(54);
    }

    private StockRoomTrait(int size){
        if(size <= 0 || size > 54){throw new IllegalArgumentException("Size must be between 1 and 54");}

        stock = Bukkit.createInventory(this,size,"stockroom");
        sellPrices = new HashMap<ItemStack, Double>();
        buyPrices = new HashMap<ItemStack, Double>();
    }

    @Override
    public void load(DataKey data) throws NPCLoadException {

        //Load the inventory
        for (DataKey slotKey : data.getRelative("inv").getIntegerSubKeys()){
            stock.setItem(
                    Integer.parseInt(slotKey.name()), ItemStorage.loadItemStack(slotKey));
        }


        //load selling prices
        for (DataKey priceKey : data.getRelative("prices").getIntegerSubKeys()){
            System.out.println("price listing found");
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));
            System.out.println(k);
            double price = priceKey.getDouble("price");
            System.out.println(price);
            sellPrices.put(k, price);
        }
        
        //load buy prices
        for (DataKey priceKey : data.getRelative("buyprices").getIntegerSubKeys()){
            System.out.println("price listing found");
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));
            System.out.println(k);
            double price = priceKey.getDouble("price");
            System.out.println(price);
            buyPrices.put(k, price);
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

        DataKey sellPriceIndex = data.getRelative("prices");
        i = 0;
        for(Entry<ItemStack,Double> price : sellPrices.entrySet()){
            if(price.getValue() > 0.0D){
                ItemStorage.saveItem(sellPriceIndex.getRelative("" + i).getRelative("item"), price.getKey());
                sellPriceIndex.getRelative("" + i++).setDouble("price", price.getValue());
            }
        }
        
        
        
        DataKey buyPriceIndex = data.getRelative("buyprices");
        i = 0;
        for(Entry<ItemStack,Double> price : sellPrices.entrySet()){
            if(price.getValue() > 0.0D){
                ItemStorage.saveItem(buyPriceIndex.getRelative("" + i).getRelative("item"), price.getKey());
                buyPriceIndex.getRelative("" + i++).setDouble("price", price.getValue());
            }
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
            if(display.contains(chk) == false && getSellPrice(is) > 0.0D){
                display.addItem(chk);
            }

        }

        return display;


    }
    
    public Inventory constructSellBox(){


        Inventory display = Bukkit.createInventory(null, 36,"Selling");

        return display;


    }
    

    /**
     * Does this stockroom contain this item
     * @param locate Item to look for
     * @param checkAmount
     * @return
     */
    public boolean hasStock(ItemStack locate,boolean checkAmount){

        ItemStack is = locate.clone();
        Material material = locate.getType();
        int amount = locate.getAmount();

        int amountFound = 0;

        for( Entry<Integer, ? extends ItemStack>  e : stock.all(material).entrySet()){
            is.setAmount(e.getValue().getAmount());
            if(e.getValue().equals(is)){
                amountFound += e.getValue().getAmount();
            }
        }
        return checkAmount ? amount <= amountFound : amountFound > 0;
    }


    public double getSellPrice(ItemStack is){
        ItemStack i = is.clone();
        i.setAmount(1);
        return sellPrices.containsKey(i) ? sellPrices.get(i) : 0;

    }

    public void setSellPrice(ItemStack is,double price){
        ItemStack i = is.clone();
        i.setAmount(1);
        sellPrices.put(i, price);

    }
    
    
    public double getBuyPrice(ItemStack is){
        ItemStack i = is.clone();
        i.setAmount(1);
        return sellPrices.containsKey(i) ? sellPrices.get(i) : 0;

    }

    public void setBuyPrice(ItemStack is,double price){
        ItemStack i = is.clone();
        i.setAmount(1);
        sellPrices.put(i, price);

    }
}
