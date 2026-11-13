scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 151 40 minecraft:air if block 52 152 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 52 151 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 151 49 minecraft:air if block 64 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 64 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 62 151 33 minecraft:air if block 62 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 62 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 53 151 49 minecraft:air if block 53 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 53 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 151 41 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 151 41 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b113 ftm_brick 2
