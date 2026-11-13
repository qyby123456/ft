scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 148 minecraft:air if block 22 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 151 158 minecraft:air if block 35 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 35 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 151 145 minecraft:air if block 37 152 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 37 151 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 151 156 minecraft:air if block 19 152 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 151 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 151 151 minecraft:air if block 30 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 30 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 144 minecraft:air if block 27 152 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 24 151 153 minecraft:air if block 24 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 24 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 151 152 minecraft:air if block 36 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 36 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b143 ftm_brick 2
