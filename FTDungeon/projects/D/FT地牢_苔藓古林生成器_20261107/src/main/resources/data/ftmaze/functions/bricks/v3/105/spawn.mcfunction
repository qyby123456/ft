scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 211 49 minecraft:air if block 193 212 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 193 211 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 211 39 minecraft:air if block 209 212 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 209 211 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 59 minecraft:air if block 206 212 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 196 211 39 minecraft:air if block 196 212 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 196 211 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 211 46 minecraft:air if block 203 212 46 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 203 211 46 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 58 minecraft:air if block 190 212 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 51 minecraft:air if block 210 212 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 211 55 minecraft:air if block 198 212 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 198 211 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 211 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 211 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b105 ftm_brick 2
