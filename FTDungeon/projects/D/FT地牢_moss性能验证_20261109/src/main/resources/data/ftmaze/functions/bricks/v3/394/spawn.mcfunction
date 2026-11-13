scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1059 151 198 minecraft:air if block 1059 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1059 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1049 151 202 minecraft:air if block 1049 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1049 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1064 151 202 minecraft:air if block 1064 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1064 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1052 151 198 minecraft:air if block 1052 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1052 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1056 151 202 minecraft:air if block 1056 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1056 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1053 151 201 minecraft:air if block 1053 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1053 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1055 151 199 minecraft:air if block 1055 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1055 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1060 151 201 minecraft:air if block 1060 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1060 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1060 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1060 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b394 ftm_brick 2
