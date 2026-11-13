scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 151 93 minecraft:air if block 178 152 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 178 151 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 104 minecraft:air if block 193 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 151 91 minecraft:air if block 191 152 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 191 151 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 151 104 minecraft:air if block 175 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 175 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 151 98 minecraft:air if block 186 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 186 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 151 100 minecraft:air if block 179 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 179 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 151 90 minecraft:air if block 183 152 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 183 151 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 151 104 minecraft:air if block 183 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 183 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 94 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 94 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b40 ftm_brick 2
