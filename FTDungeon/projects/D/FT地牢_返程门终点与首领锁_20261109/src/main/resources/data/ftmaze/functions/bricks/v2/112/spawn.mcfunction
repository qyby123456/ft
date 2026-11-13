scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 151 41 minecraft:air if block 52 152 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 52 151 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 151 52 minecraft:air if block 64 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 64 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 151 34 minecraft:air if block 63 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 63 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 52 151 52 minecraft:air if block 52 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 52 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 151 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 151 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b112 ftm_brick 2
