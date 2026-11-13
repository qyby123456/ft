scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 31 178 minecraft:air if block 206 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 206 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 31 188 minecraft:air if block 219 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 219 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 31 175 minecraft:air if block 221 32 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 221 31 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 31 186 minecraft:air if block 203 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 203 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 31 181 minecraft:air if block 214 32 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 214 31 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 31 174 minecraft:air if block 211 32 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 211 31 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 31 183 minecraft:air if block 208 32 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 208 31 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 31 182 minecraft:air if block 220 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 220 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b95 ftm_brick 2
