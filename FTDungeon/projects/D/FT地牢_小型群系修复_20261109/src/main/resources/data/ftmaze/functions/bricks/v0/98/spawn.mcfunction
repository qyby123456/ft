scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 211 41 minecraft:air if block 52 212 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 52 211 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 211 52 minecraft:air if block 64 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 64 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 211 34 minecraft:air if block 63 212 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 63 211 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 52 211 52 minecraft:air if block 52 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 52 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 211 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 211 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b98 ftm_brick 2
