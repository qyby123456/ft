scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 95 29 minecraft:air if block 641 96 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 641 95 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 95 19 minecraft:air if block 658 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 658 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 654 95 39 minecraft:air if block 654 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 654 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 95 19 minecraft:air if block 638 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 95 39 minecraft:air if block 638 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 95 22 minecraft:air if block 648 96 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 95 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 655 95 28 minecraft:air if block 655 96 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 655 95 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 95 35 minecraft:air if block 646 96 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 646 95 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 95 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 95 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b185 ftm_brick 2
