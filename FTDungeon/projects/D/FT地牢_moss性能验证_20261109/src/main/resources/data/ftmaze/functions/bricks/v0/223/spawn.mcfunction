scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1460 91 62 minecraft:air if block 1460 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1460 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1477 91 54 minecraft:air if block 1477 92 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1477 91 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1472 91 72 minecraft:air if block 1472 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1472 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1464 91 52 minecraft:air if block 1464 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1464 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1469 91 59 minecraft:air if block 1469 92 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1469 91 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1457 91 70 minecraft:air if block 1457 92 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1457 91 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1466 91 67 minecraft:air if block 1466 92 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1466 91 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1475 91 64 minecraft:air if block 1475 92 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1475 91 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1461 91 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1461 91 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b223 ftm_brick 2
