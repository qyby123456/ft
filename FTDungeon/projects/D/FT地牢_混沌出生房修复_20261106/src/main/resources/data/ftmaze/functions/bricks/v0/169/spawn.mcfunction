scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 401 151 98 minecraft:air if block 401 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 401 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 416 151 115 minecraft:air if block 416 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 416 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 398 151 114 minecraft:air if block 398 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 398 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 416 151 95 minecraft:air if block 416 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 416 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 151 107 minecraft:air if block 408 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 408 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 417 151 105 minecraft:air if block 417 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 417 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 397 151 104 minecraft:air if block 397 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 397 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 410 151 99 minecraft:air if block 410 152 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 410 151 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 401 151 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 401 151 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b169 ftm_brick 2
