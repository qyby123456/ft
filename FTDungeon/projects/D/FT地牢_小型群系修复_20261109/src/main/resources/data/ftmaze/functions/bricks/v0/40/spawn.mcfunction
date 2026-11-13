scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 151 98 minecraft:air if block 183 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 183 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 151 89 minecraft:air if block 194 152 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 194 151 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 89 minecraft:air if block 174 152 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 102 minecraft:air if block 193 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 151 102 minecraft:air if block 175 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 175 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 88 minecraft:air if block 184 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 151 94 minecraft:air if block 189 152 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 189 151 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 151 95 minecraft:air if block 177 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 177 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 151 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 151 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b40 ftm_brick 2
