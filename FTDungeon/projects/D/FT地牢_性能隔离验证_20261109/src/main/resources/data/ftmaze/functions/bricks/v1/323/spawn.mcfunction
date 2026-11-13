scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 708 151 198 minecraft:air if block 708 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 708 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 151 202 minecraft:air if block 698 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 698 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 151 202 minecraft:air if block 713 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 713 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 151 198 minecraft:air if block 701 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 701 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 151 202 minecraft:air if block 705 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 705 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 151 201 minecraft:air if block 702 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 702 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 151 199 minecraft:air if block 704 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 704 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 151 201 minecraft:air if block 709 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 709 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 709 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 709 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b323 ftm_brick 2
