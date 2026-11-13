scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 211 119 minecraft:air if block 1552 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1552 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 109 minecraft:air if block 1568 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 211 129 minecraft:air if block 1565 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1565 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 211 109 minecraft:air if block 1555 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1555 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1550 211 129 minecraft:air if block 1550 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1550 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1563 211 118 minecraft:air if block 1563 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1563 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 211 125 minecraft:air if block 1557 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1557 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 122 minecraft:air if block 1568 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1553 211 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1553 211 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b229 ftm_brick 2
