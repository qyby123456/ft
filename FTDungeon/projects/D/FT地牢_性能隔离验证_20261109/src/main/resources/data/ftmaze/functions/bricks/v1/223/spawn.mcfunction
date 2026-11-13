scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 91 72 minecraft:air if block 1444 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1444 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 91 86 minecraft:air if block 1461 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1461 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 91 66 minecraft:air if block 1459 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1459 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 91 86 minecraft:air if block 1441 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1441 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 91 80 minecraft:air if block 1451 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1451 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1455 91 73 minecraft:air if block 1455 92 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1455 91 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 91 66 minecraft:air if block 1448 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1448 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 91 78 minecraft:air if block 1459 92 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1459 91 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 91 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 91 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b223 ftm_brick 2
