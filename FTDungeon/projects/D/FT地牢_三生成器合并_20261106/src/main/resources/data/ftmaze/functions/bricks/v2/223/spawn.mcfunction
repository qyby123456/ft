scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 211 72 minecraft:air if block 1444 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1444 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 211 86 minecraft:air if block 1461 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1461 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 211 66 minecraft:air if block 1459 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1459 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 211 86 minecraft:air if block 1441 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1441 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 211 80 minecraft:air if block 1451 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1451 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1455 211 73 minecraft:air if block 1455 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1455 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 211 66 minecraft:air if block 1448 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1448 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 211 78 minecraft:air if block 1459 212 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1459 211 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 211 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 211 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b223 ftm_brick 2
