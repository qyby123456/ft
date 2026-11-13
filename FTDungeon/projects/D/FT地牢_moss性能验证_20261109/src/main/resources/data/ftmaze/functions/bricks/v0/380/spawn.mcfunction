scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1035 211 198 minecraft:air if block 1035 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1035 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1025 211 202 minecraft:air if block 1025 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1025 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 211 202 minecraft:air if block 1040 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1040 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1028 211 198 minecraft:air if block 1028 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1028 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 211 202 minecraft:air if block 1032 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1032 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 211 201 minecraft:air if block 1029 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1029 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1031 211 199 minecraft:air if block 1031 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1031 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1036 211 201 minecraft:air if block 1036 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1036 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1036 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1036 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b380 ftm_brick 2
