scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 211 66 minecraft:air if block 107 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 107 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 211 59 minecraft:air if block 96 212 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 96 211 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 211 73 minecraft:air if block 98 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 98 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 211 65 minecraft:air if block 99 212 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 99 211 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 211 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 211 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b101 ftm_brick 2
