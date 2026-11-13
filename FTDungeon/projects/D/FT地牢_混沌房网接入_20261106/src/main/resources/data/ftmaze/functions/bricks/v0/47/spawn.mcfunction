scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 217 151 177 minecraft:air if block 217 152 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 217 151 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 151 184 minecraft:air if block 203 152 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 151 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 174 minecraft:air if block 206 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 151 186 minecraft:air if block 221 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 221 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 180 minecraft:air if block 210 152 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 151 172 minecraft:air if block 213 152 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 213 151 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 151 186 minecraft:air if block 212 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 212 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 151 182 minecraft:air if block 215 152 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 215 151 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 218 151 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 218 151 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b47 ftm_brick 2
