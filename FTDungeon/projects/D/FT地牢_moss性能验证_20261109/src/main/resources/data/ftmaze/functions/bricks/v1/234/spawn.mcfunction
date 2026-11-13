scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 151 25 minecraft:air if block 1552 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1552 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 151 38 minecraft:air if block 1565 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1565 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1566 151 19 minecraft:air if block 1566 152 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1566 151 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 151 35 minecraft:air if block 1548 152 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1548 151 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 151 32 minecraft:air if block 1558 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1558 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 29 minecraft:air if block 1568 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 151 18 minecraft:air if block 1555 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1555 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 151 23 minecraft:air if block 1560 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1560 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b234 ftm_brick 2
