scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1553 151 98 minecraft:air if block 1553 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1553 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 115 minecraft:air if block 1568 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1550 151 114 minecraft:air if block 1550 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1550 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 95 minecraft:air if block 1568 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 151 107 minecraft:air if block 1560 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1560 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1569 151 105 minecraft:air if block 1569 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1569 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1549 151 104 minecraft:air if block 1549 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1549 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1562 151 99 minecraft:air if block 1562 152 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1562 151 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1553 151 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1553 151 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b229 ftm_brick 2
