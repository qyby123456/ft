scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 217 25 minecraft:air if block 641 218 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 641 217 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 217 17 minecraft:air if block 658 218 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 658 217 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 653 217 33 minecraft:air if block 653 218 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 653 217 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 217 16 minecraft:air if block 646 218 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 646 217 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 217 23 minecraft:air if block 651 218 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 651 217 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 217 33 minecraft:air if block 638 218 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 638 217 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 217 18 minecraft:air if block 638 218 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 638 217 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 217 30 minecraft:air if block 646 218 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 646 217 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 217 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 217 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b185 ftm_brick 2
