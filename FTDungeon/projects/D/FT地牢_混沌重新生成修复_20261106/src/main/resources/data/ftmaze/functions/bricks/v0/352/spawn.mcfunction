scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 987 151 198 minecraft:air if block 987 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 987 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 977 151 202 minecraft:air if block 977 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 977 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 151 202 minecraft:air if block 992 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 992 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 980 151 198 minecraft:air if block 980 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 980 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 151 202 minecraft:air if block 984 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 984 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 151 201 minecraft:air if block 981 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 981 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 983 151 199 minecraft:air if block 983 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 983 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 988 151 201 minecraft:air if block 988 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 988 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 988 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 988 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b352 ftm_brick 2
