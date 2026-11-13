scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 176 91 293 minecraft:air if block 176 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 176 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 169 91 302 minecraft:air if block 169 92 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 169 91 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 180 91 301 minecraft:air if block 180 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 180 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 169 91 294 minecraft:air if block 169 92 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 169 91 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 91 298 minecraft:air if block 173 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 173 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 303 minecraft:air if block 174 92 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 178 91 297 minecraft:air if block 178 92 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 178 91 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 168 91 298 minecraft:air if block 168 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 168 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 176 91 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 176 91 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b288 ftm_brick 2
