scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1652 151 62 minecraft:air if block 1652 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1652 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 151 77 minecraft:air if block 1668 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1668 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 151 57 minecraft:air if block 1668 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1668 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 151 77 minecraft:air if block 1648 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1648 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1658 151 71 minecraft:air if block 1658 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1658 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 151 66 minecraft:air if block 1665 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1665 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 151 57 minecraft:air if block 1657 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1657 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 151 57 minecraft:air if block 1648 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1648 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1652 151 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1652 151 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b233 ftm_brick 2
