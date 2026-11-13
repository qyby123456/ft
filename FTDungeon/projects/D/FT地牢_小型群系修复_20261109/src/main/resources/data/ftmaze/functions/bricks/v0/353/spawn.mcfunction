scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1029 151 198 minecraft:air if block 1029 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1029 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1019 151 202 minecraft:air if block 1019 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1019 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1034 151 202 minecraft:air if block 1034 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1034 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 151 198 minecraft:air if block 1022 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1022 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1026 151 202 minecraft:air if block 1026 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1026 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1023 151 201 minecraft:air if block 1023 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1023 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1025 151 199 minecraft:air if block 1025 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1025 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 151 201 minecraft:air if block 1030 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1030 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1030 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1030 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b353 ftm_brick 2
