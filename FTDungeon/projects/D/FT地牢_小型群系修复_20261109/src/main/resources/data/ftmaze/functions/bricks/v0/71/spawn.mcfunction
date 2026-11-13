scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 177 minecraft:air if block 217 92 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 91 184 minecraft:air if block 203 92 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 91 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 174 minecraft:air if block 206 92 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 91 186 minecraft:air if block 221 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 221 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 180 minecraft:air if block 210 92 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 91 172 minecraft:air if block 213 92 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 213 91 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 91 186 minecraft:air if block 212 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 212 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 91 182 minecraft:air if block 215 92 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 215 91 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 218 91 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 218 91 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b71 ftm_brick 2
