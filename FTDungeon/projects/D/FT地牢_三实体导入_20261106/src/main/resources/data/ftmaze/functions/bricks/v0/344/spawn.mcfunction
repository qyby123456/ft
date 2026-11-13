scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 651 151 198 minecraft:air if block 651 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 651 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 641 151 202 minecraft:air if block 641 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 641 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 151 202 minecraft:air if block 656 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 656 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 644 151 198 minecraft:air if block 644 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 644 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 151 202 minecraft:air if block 648 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 648 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 151 201 minecraft:air if block 645 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 645 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 647 151 199 minecraft:air if block 647 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 647 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 652 151 201 minecraft:air if block 652 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 652 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 652 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 652 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b344 ftm_brick 2
