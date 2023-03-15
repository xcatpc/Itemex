Tested MC Version: 1.19.3

Requirements: Vault

SpigotMC: https://www.spigotmc.org/resources/itemex-players-can-exchange-all-items-with-other-players-free-market.108398/

Github: https://github.com/xcatpc/Itemex

Discord: https://discord.gg/wTyjfjCmEU

STATUS: BETA - tested serveral hours with multiple players

[MEDIA=youtube]0se7owqRkic[/MEDIA]

Don't be confused about the double notification in this video. This is because I bought and sold to myself.

Short Description:
With this plugin, players can sell or buy any item on a free market, by create a sell- or buyorder. The price of each item will be determined by supply and demand. I developed this plugin for myself, but If someone will find it usefull, I will keep developing and updating.

Usage: (autocompletion in console)
/ix help

/ix price | prints the current buy and sell orders
/ix price <itemid> | prints the current buy and sell orders

/ix buy | buy what is in right hand on best price (existing sellorder needed)
/ix sell | sell what is in right hand on best price (existing buyorder needed)
 
/ix buy <itemname> <amount> <limit | market> <price> | create buy order
/ix sell <itemname> <amount> <limit | market> <price> | create sell order

/ix withdraw list | list all your available payouts
/ix withdraw <itemname> <amount> | withdraw

/ix order list <buyorders | sellorders> *<itemid> | * optional
/ix order close <buyorders | sellorders> <order id> | close an existing order

----------------------------------------------
# Roadmap: (priority in order) #
----------------------------------------------
- GUI Graphical User Interface
/ix gui | Graphical User Interface (*create limit orders)

- miscellaneous

- /ix withdraw list must have a parameter of page. only 100 entries can be send to player.
- add default prices that reflects on the reserve currency (DIAMOND) (useful if no buy and sellorders are available or only a buy or sellorder) - need statistics
- proof input of user like on /ix list everywhere. (On some commands its not checking if the values are valide)
- GUI: on /ix gui add each 3 or 4 sell and buy orders by hoover over item and sum of all available items
- GUI: filter out some blocks (like commandblock)
- GUI: sort items by availibity
- add potions and enchanted items

- Admin Function
A admin shop function creates automatically a buy and sell order for every item. If someone buys an item from the admin order, the price will increase and update the admin order. Also If a seller sells an item, the price will decrease.

- Signs
Easy use /ix buy and /ix sell by clicking right or left on a sign. If there are a lot of signs on a server could be difficult to update prices. Not sure, I have also to investigate.

- WEBUI (my favorite but a lot of work)
*This idea will be implemented after the basic plugin is stable -
Imagine you have a webfront for trading with charts and everything you need, similar to a cryptocurrency exchange. I can add to each item a icon for better user experience. And you can also trade on your smartphone without being in the game. I want to build that after, the plugin is heavly tested. Please support my project and test the plugin!


Is It easy to understand what the plugin exactly does and how it works? xcatpc@proton.me

If you want to spend a coffee: (The Plugin is and going to free forever)

Monero (XMR):
4AbefhzT7HqFMJr7wYefrmGP1pcgQn5cdBqeVK7Yrmo7QSeitoHYD9ffZUp58ixBxFSwPuvnYNrp56BaQt5moPVxSTsjwXg
