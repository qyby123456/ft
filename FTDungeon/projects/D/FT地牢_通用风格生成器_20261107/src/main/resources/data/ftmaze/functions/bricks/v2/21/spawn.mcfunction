scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 119 minecraft:air if block 206 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 211 130 minecraft:air if block 222 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 222 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 211 130 minecraft:air if block 202 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 202 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 211 117 minecraft:air if block 219 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 219 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 211 123 minecraft:air if block 214 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 214 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 211 126 minecraft:air if block 207 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 207 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 211 116 minecraft:air if block 211 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 211 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 211 123 minecraft:air if block 203 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 203 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 211 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 211 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b21 ftm_brick 2
