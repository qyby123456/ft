scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 151 95 minecraft:air if block 178 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 178 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 151 102 minecraft:air if block 194 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 194 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 151 90 minecraft:air if block 189 152 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 189 151 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 103 minecraft:air if block 182 152 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 101 minecraft:air if block 174 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 151 97 minecraft:air if block 186 152 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 186 151 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 90 minecraft:air if block 174 152 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 91 minecraft:air if block 182 152 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 94 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 94 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b40 ftm_brick 2
