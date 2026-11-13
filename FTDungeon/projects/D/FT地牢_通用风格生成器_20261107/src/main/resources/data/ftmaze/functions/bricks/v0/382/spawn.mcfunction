scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1119 211 198 minecraft:air if block 1119 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1119 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1109 211 202 minecraft:air if block 1109 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1109 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1124 211 202 minecraft:air if block 1124 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1124 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1112 211 198 minecraft:air if block 1112 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1112 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1116 211 202 minecraft:air if block 1116 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1116 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1113 211 201 minecraft:air if block 1113 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1113 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1115 211 199 minecraft:air if block 1115 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1115 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1120 211 201 minecraft:air if block 1120 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1120 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1120 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1120 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b382 ftm_brick 2
