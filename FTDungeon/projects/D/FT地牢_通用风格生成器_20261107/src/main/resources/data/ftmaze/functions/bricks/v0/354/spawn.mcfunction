scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1071 151 198 minecraft:air if block 1071 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1071 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1061 151 202 minecraft:air if block 1061 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1061 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1076 151 202 minecraft:air if block 1076 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1076 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1064 151 198 minecraft:air if block 1064 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1064 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1068 151 202 minecraft:air if block 1068 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1068 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1065 151 201 minecraft:air if block 1065 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1065 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1067 151 199 minecraft:air if block 1067 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1067 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 151 201 minecraft:air if block 1072 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1072 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1072 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1072 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b354 ftm_brick 2
