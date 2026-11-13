scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 49 minecraft:air if block 193 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 91 39 minecraft:air if block 209 92 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 91 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 59 minecraft:air if block 206 92 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 196 91 39 minecraft:air if block 196 92 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 196 91 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 91 46 minecraft:air if block 203 92 46 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 91 46 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 58 minecraft:air if block 190 92 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 51 minecraft:air if block 210 92 51 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 51 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 91 55 minecraft:air if block 198 92 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 198 91 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b133 ftm_brick 2
