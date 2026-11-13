scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1025 157 58 minecraft:air if block 1025 158 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1025 157 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 157 72 minecraft:air if block 1042 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1042 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 157 52 minecraft:air if block 1040 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 157 72 minecraft:air if block 1022 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1022 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 157 66 minecraft:air if block 1032 158 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1032 157 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1036 157 59 minecraft:air if block 1036 158 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1036 157 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 157 52 minecraft:air if block 1029 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1029 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 157 64 minecraft:air if block 1040 158 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 157 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 157 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 157 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b202 ftm_brick 2
