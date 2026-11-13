scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 91 117 minecraft:air if block 1444 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1444 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 91 108 minecraft:air if block 1461 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1461 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1457 91 127 minecraft:air if block 1457 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1457 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 91 107 minecraft:air if block 1448 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1448 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 91 114 minecraft:air if block 1454 92 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1454 91 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 91 126 minecraft:air if block 1441 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1441 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 91 123 minecraft:air if block 1449 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1449 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 91 119 minecraft:air if block 1460 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1460 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b220 ftm_brick 2
