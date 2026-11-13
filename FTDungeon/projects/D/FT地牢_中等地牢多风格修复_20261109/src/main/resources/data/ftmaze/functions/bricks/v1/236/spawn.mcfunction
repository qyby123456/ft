scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1662 151 34 minecraft:air if block 1662 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1662 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 151 21 minecraft:air if block 1648 152 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1648 151 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 151 18 minecraft:air if block 1665 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1665 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1650 151 37 minecraft:air if block 1650 152 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1650 151 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1654 151 28 minecraft:air if block 1654 152 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1654 151 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 151 27 minecraft:air if block 1668 152 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1668 151 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1656 151 19 minecraft:air if block 1656 152 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1656 151 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1661 151 24 minecraft:air if block 1661 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1661 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1663 151 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1663 151 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b236 ftm_brick 2
