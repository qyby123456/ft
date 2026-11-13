scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1329 91 198 minecraft:air if block 1329 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1329 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1319 91 202 minecraft:air if block 1319 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1319 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1334 91 202 minecraft:air if block 1334 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1334 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1322 91 198 minecraft:air if block 1322 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1322 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1326 91 202 minecraft:air if block 1326 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1326 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1323 91 201 minecraft:air if block 1323 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1323 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1325 91 199 minecraft:air if block 1325 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1325 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1330 91 201 minecraft:air if block 1330 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1330 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1330 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1330 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b387 ftm_brick 2
