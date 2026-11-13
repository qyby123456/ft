scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 708 31 198 minecraft:air if block 708 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 708 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 31 202 minecraft:air if block 698 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 698 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 31 202 minecraft:air if block 713 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 713 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 31 198 minecraft:air if block 701 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 701 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 31 202 minecraft:air if block 705 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 705 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 31 201 minecraft:air if block 702 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 702 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 31 199 minecraft:air if block 704 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 704 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 31 201 minecraft:air if block 709 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 709 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 709 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 709 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b332 ftm_brick 2
