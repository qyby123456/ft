scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 31 72 minecraft:air if block 1444 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1444 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 31 86 minecraft:air if block 1461 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1461 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 31 66 minecraft:air if block 1459 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1459 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 31 86 minecraft:air if block 1441 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1441 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 31 80 minecraft:air if block 1451 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1451 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1455 31 73 minecraft:air if block 1455 32 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1455 31 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 31 66 minecraft:air if block 1448 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1448 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 31 78 minecraft:air if block 1459 32 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1459 31 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 31 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 31 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b223 ftm_brick 2
