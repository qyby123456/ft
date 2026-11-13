scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1203 211 198 minecraft:air if block 1203 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1203 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1193 211 202 minecraft:air if block 1193 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1193 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1208 211 202 minecraft:air if block 1208 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1208 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1196 211 198 minecraft:air if block 1196 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1196 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1200 211 202 minecraft:air if block 1200 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1200 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1197 211 201 minecraft:air if block 1197 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1197 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1199 211 199 minecraft:air if block 1199 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1199 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1204 211 201 minecraft:air if block 1204 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1204 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1204 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1204 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b384 ftm_brick 2
