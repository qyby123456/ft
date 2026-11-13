scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 945 151 198 minecraft:air if block 945 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 945 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 935 151 202 minecraft:air if block 935 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 935 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 950 151 202 minecraft:air if block 950 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 950 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 938 151 198 minecraft:air if block 938 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 938 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 942 151 202 minecraft:air if block 942 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 942 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 939 151 201 minecraft:air if block 939 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 939 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 941 151 199 minecraft:air if block 941 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 941 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 946 151 201 minecraft:air if block 946 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 946 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 946 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 946 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b351 ftm_brick 2
