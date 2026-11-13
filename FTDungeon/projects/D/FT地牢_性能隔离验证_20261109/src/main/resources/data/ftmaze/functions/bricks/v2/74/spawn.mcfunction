scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 69 32 158 minecraft:air if block 69 33 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 69 32 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 32 147 minecraft:air if block 81 33 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 81 32 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 32 147 minecraft:air if block 63 33 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 63 32 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 32 162 minecraft:air if block 80 33 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 80 32 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 32 149 minecraft:air if block 72 33 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 32 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 32 162 minecraft:air if block 63 33 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 63 32 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 75 32 155 minecraft:air if block 75 33 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 75 32 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 32 154 minecraft:air if block 64 33 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 64 32 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 32 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 32 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b74 ftm_brick 2
