scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1000 31 198 minecraft:air if block 1000 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1000 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 201 minecraft:air if block 992 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 31 203 minecraft:air if block 1004 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1004 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 31 197 minecraft:air if block 994 32 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 994 31 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1000 31 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1000 31 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b339 ftm_brick 2
