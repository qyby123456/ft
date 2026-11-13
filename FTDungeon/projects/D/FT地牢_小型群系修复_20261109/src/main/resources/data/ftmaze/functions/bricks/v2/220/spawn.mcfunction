scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 211 117 minecraft:air if block 1444 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1444 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 211 108 minecraft:air if block 1461 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1461 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1457 211 127 minecraft:air if block 1457 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1457 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 211 107 minecraft:air if block 1448 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1448 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 211 114 minecraft:air if block 1454 212 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1454 211 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 211 126 minecraft:air if block 1441 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1441 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 211 123 minecraft:air if block 1449 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1449 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 211 119 minecraft:air if block 1460 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1460 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b220 ftm_brick 2
