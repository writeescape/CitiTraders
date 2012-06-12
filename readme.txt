CitiTraders 0.1 Beta 2 README

!!!WARNING!!!!!!WARNING!!!
!!!WARNING!!!!!!WARNING!!!
!!!WARNING!!!!!!WARNING!!!

CitiTraders is currently in BETA, DO NOT USE ON PRODUCTION SYSTEMS.

!!!WARNING!!!!!!WARNING!!!
!!!WARNING!!!!!!WARNING!!!
!!!WARNING!!!!!!WARNING!!!

CitiTraders provides a trading character type for Citizens 2.
This character uses vault to sell items in it's stockroom to other players.

Features:
- Simple, ingame UI
- Support for all items (inc. Enchanted items)
- (Future: Will support Village trader screen)

Installation
============
YOU MUST HAVE CITIZENS 2 INSTALLED
YOU MUST HAVE VAULT AND AN ECONOMY PLUGIN INSTALLED
Drop CitiTraders into the plugins folder.

Configuration
=============
Valid trader npc types are set in config.yml

The profiles section allows you setup multiple tiers for traders.

permission node traders.profile.profilename grants a user a profile.
default is assigned automatically, set it to 0 to prevent anyone w/o perms creating npcs.

 

Creation
========

To create a trader, use the following command.
Using Citizens:
  /npc create npcname --char trader ...Other options...
using CitiTraders (allows prices  
/trader create name -type [TYPE] 
[TYPE] is an npc type set in the config.yml

Stocking 
========
Right click a trader you own while holding a book, this will open up the stock room, place items you wish to sell in here.

Price list
==========

use /trader setprice amount

Then right click an NPC while holding the item you wish to set a price for.
NOTE: DAMAGE VALUES AND ENCHANTMENTS ARE RESPECTED, 

Trading
=======
Right click the npc, left clicking will display prices in chat, Shift clicking will move through the screens and purchase items.

The first screen displays all items in stock that have a price set. Shift clicking will select an item. Left click price of one item.

The second screen displays the selected item in various quantities (64,32,16,8,4,2,1). Shift click purchases that stack, left click shows price of that stack.

