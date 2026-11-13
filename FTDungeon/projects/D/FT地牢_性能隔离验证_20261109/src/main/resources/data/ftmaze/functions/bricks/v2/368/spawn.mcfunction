scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1011 211 198 minecraft:air if block 1011 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1011 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1001 211 202 minecraft:air if block 1001 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1001 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1016 211 202 minecraft:air if block 1016 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1016 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 211 198 minecraft:air if block 1004 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1004 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1008 211 202 minecraft:air if block 1008 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1008 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1005 211 201 minecraft:air if block 1005 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1005 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1007 211 199 minecraft:air if block 1007 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1007 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1012 211 201 minecraft:air if block 1012 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1012 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1012 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1012 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b368 ftm_brick 2
