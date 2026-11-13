scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 123 minecraft:air if block 206 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 211 130 minecraft:air if block 222 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 222 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 118 minecraft:air if block 217 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 131 minecraft:air if block 210 212 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 211 129 minecraft:air if block 202 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 202 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 211 125 minecraft:air if block 214 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 214 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 211 118 minecraft:air if block 202 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 202 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 119 minecraft:air if block 210 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 211 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 211 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b21 ftm_brick 2
