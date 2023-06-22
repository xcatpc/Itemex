<img src="https://ipfs.ome.sh/ipfs/QmPw8fp7VYfC1dx3RMpg6Be97wtmj8ZhsYtVHaeKiZF4hK">

Tested MC Version: 1.19.4

Requirements:

Vault: https://www.spigotmc.org/resources/vault.34315/

TNE: https://www.spigotmc.org/resources/the-new-economy.7805/

(Maybe also other economy plugins than TNE, please test and mail me)


Github: https://github.com/xcatpc/Itemex

Discord: https://discord.gg/wTyjfjCmEU

STATUS: BETA - tested serveral hours with multiple players

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/0se7owqRkic/0.jpg)](https://www.youtube.com/watch?v=0se7owqRkic)


Don't be confused about the double notification in this video. This is because I bought and sold to myself.

Short Description:
With this plugin, players can sell or buy any item on a free market, by create a sell- or buyorder. The price of each item will be determined by supply and demand. I developed this plugin for myself, but If someone will find it usefull, I will keep developing and updating.

Description:
The Minecraft Server Plugin ITEMEX is a powerful tool that enables players to engage in a vibrant and dynamic marketplace within their Minecraft game. With this plugin, players have the ability to buy and sell any in-game item on a free market, using either sell orders or buy orders to facilitate their transactions.

Using the ITEMEX plugin, players can create sell orders that specify the quantity of the item they want to sell, as well as the price they are willing to sell it for. Similarly, players can create buy orders that indicate the quantity of the item they want to buy, as well as the maximum price they are willing to pay for it.

One of the most interesting features of ITEMEX is that the price of each item is determined by the laws of supply and demand. This means that the more demand there is for a particular item, the higher its price will become, while an oversupply of the same item can result in a lower price.

To help players navigate this dynamic marketplace, ITEMEX provides two different order types: market orders and limit orders. A market order will buy or sell the item at the current market price. This is ideal for players who want to make a quick transaction and don't want to wait for a specific price.

On the other hand, a limit order will only buy or sell the item at a specified price. This type of order is more secure, but it requires another player to buy or sell at that price.

Using ITEMEX, players can create a truly immersive and realistic trading experience, all within the Minecraft game environment. Best of all, this plugin eliminates the need for any other marketplace plugins, making it a convenient and streamlined tool for players to use.

Usage: (autocompletion in console)
/ix help

/ix | opens the graphical user interface (alpha)

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

- GUI - Outstanding GUI (POWERFUL & EASY TO USE)

- miscellaneous

- market order confirmation. Otherwise It could be dangerous to pay extrem prices or get near to nothing if there are not much sell- or buyorders.
- create categories -> config file
- handle exception if update server not available
- old orders prioritize (Because old orders should be fulfilled first if price is equal)
- remove autocomplete price at market orders
- /ix gui load very slow because of heavy db usage -> insert all best_prices into class or hover over
- /ix gui best sell orders wrong if no at least 4 sell and 4 buy orders!
- remove sub and add 16 and insert 1.
- /ix quicksell (own gui for quickselling all items)
- /ix gui orders (list all orders) or is inside the normal /ix gui which would be better
- at ix sell: If I hold something in the hand it most be in the list on the top
- "confirm" on market order could be a good thing. ie. have to hold the product/price for 10seconds or something
- include the broker_fee (the half of it) into the buy price (sell order)
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
