scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 195 minecraft:air if block 27 92 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 91 212 minecraft:air if block 38 92 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 38 91 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 91 209 minecraft:air if block 18 92 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 18 91 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 91 204 minecraft:air if block 32 92 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 32 91 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 91 198 minecraft:air if block 38 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 38 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 91 198 minecraft:air if block 18 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 18 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 91 203 minecraft:air if block 23 92 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 23 91 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 91 201 minecraft:air if block 28 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 28 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 91 196 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 91 196 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b147 ftm_brick 2
