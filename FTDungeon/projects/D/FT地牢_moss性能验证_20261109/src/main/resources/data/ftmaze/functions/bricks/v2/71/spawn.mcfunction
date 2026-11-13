scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 178 minecraft:air if block 206 92 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 188 minecraft:air if block 219 92 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 91 175 minecraft:air if block 221 92 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 221 91 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 91 186 minecraft:air if block 203 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 91 181 minecraft:air if block 214 92 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 214 91 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 174 minecraft:air if block 211 92 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 91 183 minecraft:air if block 208 92 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 208 91 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 91 182 minecraft:air if block 220 92 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 220 91 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b71 ftm_brick 2
