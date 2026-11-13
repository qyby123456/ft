scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 35 minecraft:air if block 194 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 211 48 minecraft:air if block 209 212 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 209 211 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 211 30 minecraft:air if block 208 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 208 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 211 48 minecraft:air if block 191 212 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 191 211 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 211 36 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 211 36 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b104 ftm_brick 2
