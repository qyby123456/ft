scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 211 135 minecraft:air if block 641 212 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 641 211 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 211 148 minecraft:air if block 658 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 658 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 211 130 minecraft:air if block 656 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 211 148 minecraft:air if block 638 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 638 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 211 143 minecraft:air if block 648 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 648 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 211 129 minecraft:air if block 646 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 646 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 211 135 minecraft:air if block 651 212 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 651 211 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 211 140 minecraft:air if block 656 212 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 211 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 211 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 211 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b177 ftm_brick 2
