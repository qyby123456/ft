scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 211 48 minecraft:air if block 172 212 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 172 211 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 211 57 minecraft:air if block 184 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 184 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 211 41 minecraft:air if block 182 212 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 182 211 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 211 57 minecraft:air if block 173 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 173 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 211 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 211 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b106 ftm_brick 2
