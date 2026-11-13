scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 119 minecraft:air if block 206 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 151 130 minecraft:air if block 222 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 222 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 151 130 minecraft:air if block 202 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 202 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 151 117 minecraft:air if block 219 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 219 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 151 123 minecraft:air if block 214 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 214 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 151 126 minecraft:air if block 207 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 207 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 116 minecraft:air if block 211 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 151 123 minecraft:air if block 203 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b45 ftm_brick 2
