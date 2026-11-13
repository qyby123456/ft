scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 209 92 186 minecraft:air if block 209 93 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 92 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 92 175 minecraft:air if block 221 93 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 221 92 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 92 175 minecraft:air if block 203 93 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 92 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 92 190 minecraft:air if block 220 93 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 220 92 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 92 177 minecraft:air if block 212 93 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 212 92 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 92 190 minecraft:air if block 203 93 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 92 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 92 183 minecraft:air if block 215 93 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 215 92 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 92 182 minecraft:air if block 204 93 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 204 92 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 210 92 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 210 92 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b71 ftm_brick 2
