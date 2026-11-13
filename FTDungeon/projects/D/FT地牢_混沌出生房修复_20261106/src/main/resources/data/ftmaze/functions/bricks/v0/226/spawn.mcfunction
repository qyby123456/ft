scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1460 91 23 minecraft:air if block 1460 92 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1460 91 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1477 91 38 minecraft:air if block 1477 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1477 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1476 91 18 minecraft:air if block 1476 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1476 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1457 91 38 minecraft:air if block 1457 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1457 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1467 91 32 minecraft:air if block 1467 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1467 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1470 91 24 minecraft:air if block 1470 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1470 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1465 91 18 minecraft:air if block 1465 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1465 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1475 91 29 minecraft:air if block 1475 92 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1475 91 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1461 91 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1461 91 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b226 ftm_brick 2
