scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 43 minecraft:air if block 193 152 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 33 minecraft:air if block 210 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 53 minecraft:air if block 206 152 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 33 minecraft:air if block 190 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 53 minecraft:air if block 190 152 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 200 151 36 minecraft:air if block 200 152 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 200 151 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 151 42 minecraft:air if block 207 152 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 207 151 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 151 49 minecraft:air if block 198 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 198 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 151 43 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 151 43 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b119 ftm_brick 2
