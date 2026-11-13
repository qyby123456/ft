scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 196 151 60 minecraft:air if block 196 152 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 196 151 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 151 74 minecraft:air if block 209 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 73 minecraft:air if block 190 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 151 58 minecraft:air if block 209 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 151 68 minecraft:air if block 201 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 201 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 63 minecraft:air if block 190 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 151 74 minecraft:air if block 198 152 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 198 151 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 151 62 minecraft:air if block 204 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 204 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 197 151 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 197 151 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b122 ftm_brick 2
