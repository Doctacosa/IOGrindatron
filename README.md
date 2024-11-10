# IOGrindatron

![Logo](https://www.interordi.com/images/plugins/iogrindatron-96.png)

Grindatron minigame in Minecraft, as featured on the [Creeper's Lab](https://www.creeperslab.net/).

Every four hours, a target is generated: for example, 1 x elytra, 2 x snowball, 8 x stone, etc. The players must then gather these materials and place them in an enderchest placed somewhere, ideally at the spawn point of the world. Successfully providing the set item gets them a point!

This means there are six potential targets to complete every day, keeping the players busy. A scoreboard is displayed to show how many targets each logged in player has succesfully accomplished. A limited amount of energy given forces the players to strategize their moves.

To force people to return to the spawn point, regular players aren't allowed to place their own enderchests.

This event is designed to run for multiples consecutive days. Using a full month before a reset is the suggested duration.


## How to play

To understand how this is played from a player's perspective, [see this guide on our wiki](https://wiki.creeperslab.net/worlds/kenorland/grindatron). Note that Merit Points are exclusive to the Creeper's Lab, you're free to implement your own rewards as none are built-in. Completed cycles for each player can be found in the `grindatron__cycles_players` table.


## Setup guide

1. Download the plugin and place it in the `plugins/` directory of the server.
2. Start and stop the server to create the configuration files.
3. Edit `plugins/IOGrindatron/config.yml` to set your settings, described below.
4. Place an enderchest near spawn, where people will be able to submit their targets.
5. You can edit the content of `grindatron__possible_targets` to add or remove targets as you see fit.
6. To start a new game cycle:
   1. Stop the server.
   2. Empty the tables `grindatron__cycles`, `grindatron__cycles_players`, `grindatron__players`, `grindatron__players_daily`
   3. Restart the server.


## Configuration

`database.host`: Database host  
`database.port`: Database port  
`database.base`: Database name  
`database.username`: Database username  
`database.password`: Database password  

Progression gates, indicating how many targets have to be completed before the players are allowed to do something:
`gates.access-nether`: Access the Nether
`gates.access-end`: Access the End
`gates.craft-diamond`: Craft diamond tools and equipment
`gates.craft-netherite`: Craft netherite tools and equipment
`gates.equip-diamond`: Equip diamond tools and equipment
`gates.equip-netherite`: Equip netherite tools and equipment
`gates.equip-elytra`: Equip an elytra


## Commands

`/target`: Get the target of the current cycle


## Permissions

None
