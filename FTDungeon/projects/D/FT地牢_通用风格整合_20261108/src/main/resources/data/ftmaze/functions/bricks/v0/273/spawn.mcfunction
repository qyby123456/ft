scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 44 211 186 minecraft:air if block 44 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 44 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 211 189 minecraft:air if block 36 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 36 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 48 211 191 minecraft:air if block 48 212 191 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 48 211 191 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 211 185 minecraft:air if block 38 212 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 38 211 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 44 211 185 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 44 211 185 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b273 ftm_brick 2
