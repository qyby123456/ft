scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 177 91 180 minecraft:air if block 177 92 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 177 91 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 91 189 minecraft:air if block 191 92 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 191 91 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 91 177 minecraft:air if block 188 92 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 188 91 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 91 187 minecraft:air if block 181 92 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 181 91 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 180 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 180 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b67 ftm_brick 2
