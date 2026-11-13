scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 151 148 minecraft:air if block 65 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 65 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 151 158 minecraft:air if block 81 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 81 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 147 minecraft:air if block 78 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 68 151 158 minecraft:air if block 68 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 68 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 151 152 minecraft:air if block 72 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 72 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 151 155 minecraft:air if block 63 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 63 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 146 minecraft:air if block 71 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 157 minecraft:air if block 74 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b26 ftm_brick 2
