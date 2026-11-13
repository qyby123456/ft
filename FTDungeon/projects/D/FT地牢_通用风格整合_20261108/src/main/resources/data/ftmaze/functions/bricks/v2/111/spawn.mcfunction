scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 151 43 minecraft:air if block 73 152 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 73 151 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 33 minecraft:air if block 90 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 86 151 53 minecraft:air if block 86 152 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 86 151 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 33 minecraft:air if block 70 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 53 minecraft:air if block 70 152 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 151 36 minecraft:air if block 80 152 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 80 151 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 87 151 42 minecraft:air if block 87 152 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 87 151 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 49 minecraft:air if block 78 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 151 43 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 151 43 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b111 ftm_brick 2
