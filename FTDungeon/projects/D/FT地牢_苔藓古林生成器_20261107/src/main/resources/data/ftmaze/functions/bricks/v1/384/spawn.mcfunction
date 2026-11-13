scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1035 151 198 minecraft:air if block 1035 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1035 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1025 151 202 minecraft:air if block 1025 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1025 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 151 202 minecraft:air if block 1040 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1028 151 198 minecraft:air if block 1028 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1028 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 151 202 minecraft:air if block 1032 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1032 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 151 201 minecraft:air if block 1029 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1029 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1031 151 199 minecraft:air if block 1031 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1031 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1036 151 201 minecraft:air if block 1036 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1036 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1036 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1036 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b384 ftm_brick 2
