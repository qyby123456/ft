scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 217 58 minecraft:air if block 641 218 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 641 217 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 217 72 minecraft:air if block 658 218 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 658 217 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 217 52 minecraft:air if block 656 218 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 217 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 217 72 minecraft:air if block 638 218 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 638 217 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 217 66 minecraft:air if block 648 218 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 648 217 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 652 217 59 minecraft:air if block 652 218 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 652 217 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 217 52 minecraft:air if block 645 218 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 645 217 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 217 64 minecraft:air if block 656 218 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 217 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 217 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 217 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b182 ftm_brick 2
