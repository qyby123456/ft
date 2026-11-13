scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1552 91 119 minecraft:air if block 1552 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1552 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 91 109 minecraft:air if block 1568 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1568 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1565 91 129 minecraft:air if block 1565 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1565 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1555 91 109 minecraft:air if block 1555 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1555 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1550 91 129 minecraft:air if block 1550 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1550 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1563 91 118 minecraft:air if block 1563 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1563 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 91 125 minecraft:air if block 1557 92 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1557 91 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 91 122 minecraft:air if block 1568 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1568 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1553 91 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1553 91 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b229 ftm_brick 2
