scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 151 150 minecraft:air if block 66 152 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 66 151 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 151 160 minecraft:air if block 79 152 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 79 151 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 151 147 minecraft:air if block 81 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 81 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 151 158 minecraft:air if block 63 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 63 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 153 minecraft:air if block 74 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 146 minecraft:air if block 71 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 68 151 155 minecraft:air if block 68 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 68 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 151 154 minecraft:air if block 80 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 80 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b26 ftm_brick 2
