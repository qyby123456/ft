scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 98 151 186 minecraft:air if block 98 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 98 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 151 183 minecraft:air if block 109 152 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 151 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 194 minecraft:air if block 94 152 194 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 194 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 151 192 minecraft:air if block 103 152 192 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 103 151 192 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 151 183 minecraft:air if block 91 152 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 151 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 151 182 minecraft:air if block 102 152 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 102 151 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 151 191 minecraft:air if block 97 152 191 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 151 191 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 151 185 minecraft:air if block 105 152 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 105 151 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 99 151 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 99 151 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b31 ftm_brick 2
