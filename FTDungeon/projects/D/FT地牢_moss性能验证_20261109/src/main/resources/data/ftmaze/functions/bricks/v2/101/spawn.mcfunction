scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 15 minecraft:air if block 194 212 15 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 15 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 24 minecraft:air if block 206 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 211 16 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 211 16 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b101 ftm_brick 2
