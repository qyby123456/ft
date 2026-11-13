scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 195 minecraft:air if block 27 212 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 212 minecraft:air if block 38 212 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 211 209 minecraft:air if block 18 212 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 18 211 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 211 204 minecraft:air if block 32 212 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 32 211 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 198 minecraft:air if block 38 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 211 198 minecraft:air if block 18 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 18 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 211 203 minecraft:air if block 23 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 23 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 211 201 minecraft:air if block 28 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 28 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 211 196 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 211 196 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b141 ftm_brick 2
