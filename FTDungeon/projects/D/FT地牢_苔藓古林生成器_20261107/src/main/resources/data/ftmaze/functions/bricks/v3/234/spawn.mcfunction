scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 91 25 minecraft:air if block 1552 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1552 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 91 38 minecraft:air if block 1565 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1565 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1566 91 19 minecraft:air if block 1566 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1566 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 91 35 minecraft:air if block 1548 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1548 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 91 32 minecraft:air if block 1558 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1558 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 91 29 minecraft:air if block 1568 92 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1568 91 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 91 18 minecraft:air if block 1555 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1555 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 91 23 minecraft:air if block 1560 92 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1560 91 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 91 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 91 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b234 ftm_brick 2
