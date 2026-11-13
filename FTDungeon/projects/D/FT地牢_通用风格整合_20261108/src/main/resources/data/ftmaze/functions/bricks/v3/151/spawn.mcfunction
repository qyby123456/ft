scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 133 31 270 minecraft:air if block 133 32 270 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 133 31 270 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 260 minecraft:air if block 150 32 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 280 minecraft:air if block 146 32 280 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 280 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 260 minecraft:air if block 130 32 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 280 minecraft:air if block 130 32 280 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 280 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 140 31 263 minecraft:air if block 140 32 263 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 140 31 263 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 269 minecraft:air if block 147 32 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 138 31 276 minecraft:air if block 138 32 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 138 31 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 31 270 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 31 270 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b151 ftm_brick 2
