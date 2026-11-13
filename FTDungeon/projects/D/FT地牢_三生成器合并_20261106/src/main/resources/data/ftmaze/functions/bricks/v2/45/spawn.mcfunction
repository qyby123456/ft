scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 123 minecraft:air if block 206 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 151 130 minecraft:air if block 222 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 222 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 151 118 minecraft:air if block 217 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 217 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 131 minecraft:air if block 210 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 151 129 minecraft:air if block 202 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 202 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 151 125 minecraft:air if block 214 152 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 214 151 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 151 118 minecraft:air if block 202 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 202 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 119 minecraft:air if block 210 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b45 ftm_brick 2
