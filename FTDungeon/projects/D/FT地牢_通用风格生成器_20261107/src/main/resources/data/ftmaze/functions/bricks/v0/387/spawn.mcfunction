scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1329 211 198 minecraft:air if block 1329 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1329 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1319 211 202 minecraft:air if block 1319 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1319 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1334 211 202 minecraft:air if block 1334 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1334 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1322 211 198 minecraft:air if block 1322 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1322 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1326 211 202 minecraft:air if block 1326 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1326 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1323 211 201 minecraft:air if block 1323 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1323 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1325 211 199 minecraft:air if block 1325 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1325 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1330 211 201 minecraft:air if block 1330 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1330 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1330 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1330 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b387 ftm_brick 2
