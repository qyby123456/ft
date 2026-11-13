scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1652 211 62 minecraft:air if block 1652 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1652 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 211 77 minecraft:air if block 1668 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1668 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 211 57 minecraft:air if block 1668 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1668 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 211 77 minecraft:air if block 1648 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1648 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1658 211 71 minecraft:air if block 1658 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1658 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 211 66 minecraft:air if block 1665 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1665 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 211 57 minecraft:air if block 1657 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1657 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 211 57 minecraft:air if block 1648 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1648 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1652 211 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1652 211 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b233 ftm_brick 2
