scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 23 211 147 minecraft:air if block 23 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 23 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 211 157 minecraft:air if block 33 212 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 33 211 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 211 147 minecraft:air if block 34 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 34 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 211 157 minecraft:air if block 23 212 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 23 211 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 211 147 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 211 147 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b140 ftm_brick 2
