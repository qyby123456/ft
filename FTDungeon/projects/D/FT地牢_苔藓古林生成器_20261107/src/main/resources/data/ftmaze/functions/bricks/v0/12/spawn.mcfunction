scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 211 94 minecraft:air if block 150 212 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 150 211 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 164 211 105 minecraft:air if block 164 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 164 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 211 92 minecraft:air if block 162 212 92 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 162 211 92 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 148 211 105 minecraft:air if block 148 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 148 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b12 ftm_brick 2
