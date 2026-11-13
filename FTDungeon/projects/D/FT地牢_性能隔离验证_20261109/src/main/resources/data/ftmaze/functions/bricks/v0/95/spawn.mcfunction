scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 209 32 186 minecraft:air if block 209 33 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 209 32 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 32 175 minecraft:air if block 221 33 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 221 32 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 32 175 minecraft:air if block 203 33 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 203 32 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 32 190 minecraft:air if block 220 33 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 220 32 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 32 177 minecraft:air if block 212 33 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 212 32 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 32 190 minecraft:air if block 203 33 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 203 32 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 32 183 minecraft:air if block 215 33 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 215 32 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 32 182 minecraft:air if block 204 33 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 204 32 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 210 32 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 210 32 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b95 ftm_brick 2
