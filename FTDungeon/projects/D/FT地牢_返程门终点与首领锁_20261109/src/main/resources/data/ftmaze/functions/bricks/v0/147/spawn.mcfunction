scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 198 minecraft:air if block 22 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 91 205 minecraft:air if block 38 92 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 38 91 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 91 193 minecraft:air if block 33 92 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 33 91 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 91 206 minecraft:air if block 26 92 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 26 91 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 91 204 minecraft:air if block 18 92 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 18 91 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 91 200 minecraft:air if block 30 92 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 30 91 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 91 193 minecraft:air if block 18 92 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 18 91 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 91 194 minecraft:air if block 26 92 194 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 26 91 194 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b147 ftm_brick 2
