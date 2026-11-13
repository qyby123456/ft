scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1551 91 63 minecraft:air if block 1551 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1551 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 91 77 minecraft:air if block 1568 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1568 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 91 57 minecraft:air if block 1568 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1568 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 91 77 minecraft:air if block 1548 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1548 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1558 91 71 minecraft:air if block 1558 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1558 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 91 66 minecraft:air if block 1565 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1565 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 91 57 minecraft:air if block 1557 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1557 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1548 91 57 minecraft:air if block 1548 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1548 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1552 91 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1552 91 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b231 ftm_brick 2
