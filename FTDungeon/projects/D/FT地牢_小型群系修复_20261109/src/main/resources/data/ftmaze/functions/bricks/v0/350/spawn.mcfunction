scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 903 151 198 minecraft:air if block 903 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 903 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 893 151 202 minecraft:air if block 893 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 893 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 908 151 202 minecraft:air if block 908 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 908 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 896 151 198 minecraft:air if block 896 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 896 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 151 202 minecraft:air if block 900 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 900 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 151 201 minecraft:air if block 897 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 897 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 151 199 minecraft:air if block 899 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 899 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 904 151 201 minecraft:air if block 904 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 904 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 904 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 904 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b350 ftm_brick 2
