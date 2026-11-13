scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 207 minecraft:air if block 70 152 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 197 minecraft:air if block 74 152 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 212 minecraft:air if block 74 152 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 200 minecraft:air if block 70 152 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 204 minecraft:air if block 74 152 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 211 minecraft:air if block 70 152 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 203 minecraft:air if block 71 152 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 151 209 minecraft:air if block 72 152 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 72 151 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 151 208 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 151 208 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b278 ftm_brick 2
