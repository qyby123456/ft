scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 76 151 60 minecraft:air if block 76 152 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 76 151 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 151 74 minecraft:air if block 89 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 89 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 73 minecraft:air if block 70 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 151 58 minecraft:air if block 89 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 89 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 151 68 minecraft:air if block 81 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 81 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 63 minecraft:air if block 70 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 74 minecraft:air if block 78 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 84 151 62 minecraft:air if block 84 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 84 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 77 151 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 77 151 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b116 ftm_brick 2
