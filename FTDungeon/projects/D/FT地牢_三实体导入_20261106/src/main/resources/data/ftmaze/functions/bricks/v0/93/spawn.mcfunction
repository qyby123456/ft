scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 31 119 minecraft:air if block 206 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 206 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 31 130 minecraft:air if block 222 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 222 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 31 130 minecraft:air if block 202 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 202 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 31 117 minecraft:air if block 219 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 219 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 31 123 minecraft:air if block 214 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 214 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 31 126 minecraft:air if block 207 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 207 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 31 116 minecraft:air if block 211 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 211 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 31 123 minecraft:air if block 203 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 203 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b93 ftm_brick 2
