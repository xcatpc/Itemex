Tested MC Version: 1.19.3
Requirements: Vault
SpigotMC: https://www.spigotmc.org/resources/itemex-players-can-exchange-all-items-with-other-players-free-market.108398/

Short Description:
With this plugin, players can sell or buy any item on a free market, by create a sell- or buyorder. The price of each item will be determined by supply and demand. I developed this plugin for myself, but If someone will find it usefull, I will keep developing and updating.

Usage: (autocompletion in konsole)
/ix help
/ix price | prints the current buy and sell orders

/ix price <itemid> | prints the current buy and sell orders
  
/ix buy | buy what is in right hand on best price (existing sellorder needed)
  
/ix sell | sell what is in right hand on best price (existing buyorder needed)
  
/ix buy <itemname> <amount> <limit/market> <price> | create buy order
  
/ix sell <itemname> <amount> <limit/market> <price> | create sell order
  
*market is not implemented

/ix withdraw list | list all your available payouts
  
/ix withdraw <itemname> <amount> | withdraw
  
/ix gui | Graphical User Interface (*create limit orders)
  
*after implementation of market it will be switched to it
  

TO DO:

* not implemented commands
/ix order list | list all own buy- and sellorders
/ix order edit <order id> <price> | edit the price of an existing order
/ix order close <order id> | close an order

Litte Roadmap: (I will give most priority to feedbacks)
- Admin Function
A admin shop function which creates automatically a buy and sell order for every item with an amount of 1. If someone buys an item of the admin order, the price will increase and adjust the order. Also If a seller sells an item, the price will decrease. This would be my suggestion to make the admin shop as unattractive as possible to give much room for free market. This admin function could be usefull if players want to buy or sell something and no other buy or sellorders are available.

- GUI (DONE)
Its seems that It's not possible to use the creative inventory with the search field for our ITEMEX GUI.
Instead I can create a graphical user interface with an inventory of 6*9 fields.
For (at the moment) existing 1327 items would take at a 6*9-2 (subtract 2 buttons) 25.51923 sites. Thats browseable.

- Signs
Easy use /ix buy and /ix sell by clicking right or left on a sign. If there are a lot of signs on a server could be difficult to update prices. Not sure, I have also to investigate.

- Plugin folder (very basic)
create a folder for the plugin and insert the database into it. Also create a config file and also a lang files. Mysql would be also good to implement.

- WEBUI (my favorite)
Imagine you have a webfront for trading with charts and everything you need, similar to a cryptocurrency exchange. I can add to each item a icon for better user experience. And you can also trade on your smartphone without being in the game.
