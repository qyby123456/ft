scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 211 100 minecraft:air if block 641 212 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 641 211 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 211 115 minecraft:air if block 658 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 658 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 657 211 95 minecraft:air if block 657 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 657 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 211 115 minecraft:air if block 638 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 638 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 211 109 minecraft:air if block 648 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 648 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 211 101 minecraft:air if block 651 212 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 651 211 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 211 95 minecraft:air if block 646 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 646 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 211 106 minecraft:air if block 656 212 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 211 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 211 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 211 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b178 ftm_brick 2
