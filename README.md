<img src="https://ipfs.ome.sh/ipfs/QmPw8fp7VYfC1dx3RMpg6Be97wtmj8ZhsYtVHaeKiZF4hK">

Tested MC Version: 1.20.1

Tested MC Version: 1.20.1
Requirements:
Vault: https://www.spigotmc.org/resources/vault.34315/
TNE: https://www.spigotmc.org/resources/the-new-economy.7805/

Github: https://github.com/xcatpc/Itemex
Discord: https://discord.gg/wTyjfjCmEU (english, german)

Description in: DE , ES , FR , CN , RU
This plugin is multilingual at 0.20.1

ITEMEX: Realistic Economic Simulation and Dynamic Marketplace for Minecraft​

Dynamic Marketplace: ITEMEX is an innovative Minecraft server plugin that creates a dynamic and realistic marketplace within the game. It allows players to buy and sell any in-game items in a free market, enriching the gameplay.

Supply and Demand: At the heart of ITEMEX, the principles of supply and demand determine the price of each item. This promotes strategic thinking and market research, while encouraging players to efficiently manage and allocate resources.

Flexible Trading Orders: With the ability to create sell or buy orders and choose between market orders for quick transactions and limit orders for targeted trading, ITEMEX provides a flexible trading platform for all players.

Player Retention and Appeal to New Players: ITEMEX enhances player retention through a deeper economic simulation and makes the server more appealing to new players with its sophisticated and engaging trading system.

Trading Profits and Losses: Through strategic trading and market research, players can make profits with ITEMEX. But as in real life, they can also incur losses, making the game even more realistic and challenging.

Optimized Server Economy: For server administrators, ITEMEX offers the ability to understand and analyze the economy of their server. This can contribute to optimizing the player experience and improving the overall economy of the server.

Resource Allocation and Signaling: ITEMEX creates incentives for efficient resource allocation and signals which items are in high demand through price fluctuations. This promotes strategic planning and resource management.

Free Market with Autonomy: Despite the possibility of intervention, the core principle of ITEMEX remains a free market. This respects the autonomy of players and ensures a realistic trading experience.

With ITEMEX, players and server administrators can reach a new level of economic simulation in Minecraft, elevating the gaming experience to a whole new level.

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
