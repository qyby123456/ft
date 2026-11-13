scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 43 31 138 minecraft:air if block 43 32 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 43 31 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 31 141 minecraft:air if block 35 32 141 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 35 31 141 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 47 31 143 minecraft:air if block 47 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 47 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 31 137 minecraft:air if block 37 32 137 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 37 31 137 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 43 31 137 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 43 31 137 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b271 ftm_brick 2
