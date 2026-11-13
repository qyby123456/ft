scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 31 117 minecraft:air if block 1444 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1444 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 31 108 minecraft:air if block 1461 32 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1461 31 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1457 31 127 minecraft:air if block 1457 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1457 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 31 107 minecraft:air if block 1448 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1448 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 31 114 minecraft:air if block 1454 32 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1454 31 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 31 126 minecraft:air if block 1441 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1441 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 31 123 minecraft:air if block 1449 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1449 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 31 119 minecraft:air if block 1460 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1460 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b220 ftm_brick 2
