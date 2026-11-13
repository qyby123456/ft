scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 151 119 minecraft:air if block 1552 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1552 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 109 minecraft:air if block 1568 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 151 129 minecraft:air if block 1565 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1565 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 151 109 minecraft:air if block 1555 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1555 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1550 151 129 minecraft:air if block 1550 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1550 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1563 151 118 minecraft:air if block 1563 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1563 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 151 125 minecraft:air if block 1557 152 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1557 151 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 122 minecraft:air if block 1568 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1553 151 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1553 151 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b229 ftm_brick 2
