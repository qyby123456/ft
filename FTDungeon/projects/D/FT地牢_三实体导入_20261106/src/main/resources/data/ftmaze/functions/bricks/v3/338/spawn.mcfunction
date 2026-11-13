scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 960 151 198 minecraft:air if block 960 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 960 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 950 151 202 minecraft:air if block 950 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 950 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 151 202 minecraft:air if block 965 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 965 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 953 151 198 minecraft:air if block 953 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 953 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 957 151 202 minecraft:air if block 957 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 957 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 954 151 201 minecraft:air if block 954 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 954 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 151 199 minecraft:air if block 956 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 956 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 961 151 201 minecraft:air if block 961 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 961 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 961 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 961 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b338 ftm_brick 2
