scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 211 36 minecraft:air if block 172 212 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 172 211 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 211 44 minecraft:air if block 184 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 184 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 211 30 minecraft:air if block 181 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 181 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 44 minecraft:air if block 174 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 211 37 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 211 37 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b105 ftm_brick 2
