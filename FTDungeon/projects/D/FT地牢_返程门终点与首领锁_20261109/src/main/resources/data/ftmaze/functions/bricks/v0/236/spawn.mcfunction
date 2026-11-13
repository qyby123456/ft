scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1662 211 34 minecraft:air if block 1662 212 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1662 211 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 211 21 minecraft:air if block 1648 212 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1648 211 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 211 18 minecraft:air if block 1665 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1665 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1650 211 37 minecraft:air if block 1650 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1650 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1654 211 28 minecraft:air if block 1654 212 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1654 211 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 211 27 minecraft:air if block 1668 212 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1668 211 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1656 211 19 minecraft:air if block 1656 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1656 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1661 211 24 minecraft:air if block 1661 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1661 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1663 211 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1663 211 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b236 ftm_brick 2
