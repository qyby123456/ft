scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1011 31 198 minecraft:air if block 1011 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1011 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1001 31 202 minecraft:air if block 1001 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1001 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1016 31 202 minecraft:air if block 1016 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1016 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 31 198 minecraft:air if block 1004 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1004 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1008 31 202 minecraft:air if block 1008 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1008 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1005 31 201 minecraft:air if block 1005 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1005 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1007 31 199 minecraft:air if block 1007 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1007 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1012 31 201 minecraft:air if block 1012 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1012 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1012 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1012 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b367 ftm_brick 2
