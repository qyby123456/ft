scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1143 211 198 minecraft:air if block 1143 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1143 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1133 211 202 minecraft:air if block 1133 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1133 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1148 211 202 minecraft:air if block 1148 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1148 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1136 211 198 minecraft:air if block 1136 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1136 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1140 211 202 minecraft:air if block 1140 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1140 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1137 211 201 minecraft:air if block 1137 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1137 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1139 211 199 minecraft:air if block 1139 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1139 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1144 211 201 minecraft:air if block 1144 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1144 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1144 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1144 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b400 ftm_brick 2
