scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 215 91 61 minecraft:air if block 215 92 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 215 91 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 229 91 52 minecraft:air if block 229 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 229 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 226 91 66 minecraft:air if block 226 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 226 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 52 minecraft:air if block 217 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 216 91 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 216 91 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b137 ftm_brick 2
