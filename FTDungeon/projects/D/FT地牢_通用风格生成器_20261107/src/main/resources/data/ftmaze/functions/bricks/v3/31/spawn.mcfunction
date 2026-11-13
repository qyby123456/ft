scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 151 176 minecraft:air if block 93 152 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 93 151 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 151 186 minecraft:air if block 109 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 151 175 minecraft:air if block 106 152 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 106 151 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 186 minecraft:air if block 96 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 151 180 minecraft:air if block 100 152 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 100 151 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 151 183 minecraft:air if block 91 152 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 151 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 151 174 minecraft:air if block 99 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 99 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 151 185 minecraft:air if block 102 152 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 102 151 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 151 176 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 151 176 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b31 ftm_brick 2
