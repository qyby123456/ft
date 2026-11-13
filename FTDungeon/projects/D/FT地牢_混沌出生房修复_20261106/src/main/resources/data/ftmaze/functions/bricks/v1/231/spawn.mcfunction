scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1551 151 63 minecraft:air if block 1551 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1551 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 77 minecraft:air if block 1568 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 57 minecraft:air if block 1568 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 151 77 minecraft:air if block 1548 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1548 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 151 71 minecraft:air if block 1558 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1558 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 151 66 minecraft:air if block 1565 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1565 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 151 57 minecraft:air if block 1557 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1557 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 151 57 minecraft:air if block 1548 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1548 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 151 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 151 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b231 ftm_brick 2
