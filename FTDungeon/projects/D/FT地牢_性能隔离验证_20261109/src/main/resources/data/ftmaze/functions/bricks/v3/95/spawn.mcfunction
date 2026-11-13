scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 217 31 177 minecraft:air if block 217 32 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 217 31 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 31 184 minecraft:air if block 203 32 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 203 31 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 31 174 minecraft:air if block 206 32 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 206 31 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 31 186 minecraft:air if block 221 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 221 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 31 180 minecraft:air if block 210 32 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 210 31 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 31 172 minecraft:air if block 213 32 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 213 31 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 31 186 minecraft:air if block 212 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 212 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 31 182 minecraft:air if block 215 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 215 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 218 31 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 218 31 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b95 ftm_brick 2
