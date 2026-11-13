scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1551 211 63 minecraft:air if block 1551 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1551 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 77 minecraft:air if block 1568 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 57 minecraft:air if block 1568 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 211 77 minecraft:air if block 1548 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1548 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 211 71 minecraft:air if block 1558 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1558 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 211 66 minecraft:air if block 1565 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1565 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 211 57 minecraft:air if block 1557 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1557 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 211 57 minecraft:air if block 1548 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1548 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 211 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 211 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b231 ftm_brick 2
