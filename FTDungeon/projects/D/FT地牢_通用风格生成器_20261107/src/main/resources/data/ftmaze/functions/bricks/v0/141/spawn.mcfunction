scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 205 minecraft:air if block 27 212 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 211 193 minecraft:air if block 32 212 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 32 211 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 211 205 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 211 205 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b141 ftm_brick 2
