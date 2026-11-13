scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 208 minecraft:air if block 70 152 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 198 minecraft:air if block 74 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 213 minecraft:air if block 74 152 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 201 minecraft:air if block 70 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 205 minecraft:air if block 74 152 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 212 minecraft:air if block 70 152 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 204 minecraft:air if block 71 152 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 151 210 minecraft:air if block 72 152 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 72 151 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 151 209 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 151 209 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b280 ftm_brick 2
