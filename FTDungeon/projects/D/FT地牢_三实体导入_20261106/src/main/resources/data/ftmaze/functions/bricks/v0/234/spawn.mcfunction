scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 211 25 minecraft:air if block 1552 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1552 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 211 38 minecraft:air if block 1565 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1565 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1566 211 19 minecraft:air if block 1566 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1566 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 211 35 minecraft:air if block 1548 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1548 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 211 32 minecraft:air if block 1558 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1558 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 29 minecraft:air if block 1568 212 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 211 18 minecraft:air if block 1555 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1555 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 211 23 minecraft:air if block 1560 212 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1560 211 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 211 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 211 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b234 ftm_brick 2
