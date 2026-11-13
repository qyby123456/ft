scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 176 minecraft:air if block 205 152 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 151 186 minecraft:air if block 221 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 221 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 175 minecraft:air if block 218 152 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 151 186 minecraft:air if block 208 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 208 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 151 180 minecraft:air if block 212 152 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 212 151 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 151 183 minecraft:air if block 203 152 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 151 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 174 minecraft:air if block 211 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 151 185 minecraft:air if block 214 152 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 214 151 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 176 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 176 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b47 ftm_brick 2
