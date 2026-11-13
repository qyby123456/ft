scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1662 91 34 minecraft:air if block 1662 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1662 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 91 21 minecraft:air if block 1648 92 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1648 91 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 91 18 minecraft:air if block 1665 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1665 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1650 91 37 minecraft:air if block 1650 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1650 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1654 91 28 minecraft:air if block 1654 92 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1654 91 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 91 27 minecraft:air if block 1668 92 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1668 91 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1656 91 19 minecraft:air if block 1656 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1656 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1661 91 24 minecraft:air if block 1661 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1661 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1663 91 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1663 91 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b236 ftm_brick 2
