scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 147 minecraft:air if block 22 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 151 158 minecraft:air if block 38 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 38 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 151 158 minecraft:air if block 18 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 18 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 151 145 minecraft:air if block 35 152 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 35 151 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 151 151 minecraft:air if block 30 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 30 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 154 minecraft:air if block 23 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 144 minecraft:air if block 27 152 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 151 151 minecraft:air if block 19 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b143 ftm_brick 2
