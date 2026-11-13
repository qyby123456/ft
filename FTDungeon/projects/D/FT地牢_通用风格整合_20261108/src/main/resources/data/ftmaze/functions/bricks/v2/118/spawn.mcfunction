scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 42 minecraft:air if block 193 152 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 151 32 minecraft:air if block 208 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 208 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 52 minecraft:air if block 205 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 43 minecraft:air if block 210 152 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 195 151 32 minecraft:air if block 195 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 195 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 151 38 minecraft:air if block 201 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 201 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 50 minecraft:air if block 190 152 50 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 50 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 199 151 47 minecraft:air if block 199 152 47 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 199 151 47 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 151 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 151 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b118 ftm_brick 2
