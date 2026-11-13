scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 43 minecraft:air if block 193 92 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 33 minecraft:air if block 210 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 53 minecraft:air if block 206 92 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 33 minecraft:air if block 190 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 53 minecraft:air if block 190 92 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 200 91 36 minecraft:air if block 200 92 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 200 91 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 42 minecraft:air if block 207 92 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 198 91 49 minecraft:air if block 198 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 198 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 43 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 43 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b133 ftm_brick 2
