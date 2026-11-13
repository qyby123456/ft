scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 151 177 minecraft:air if block 150 152 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 150 151 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 151 188 minecraft:air if block 165 152 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 165 151 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 151 175 minecraft:air if block 163 152 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 163 151 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 151 188 minecraft:air if block 147 152 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 147 151 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 151 182 minecraft:air if block 158 152 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 158 151 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 151 184 minecraft:air if block 151 152 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 151 151 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 174 minecraft:air if block 155 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 188 minecraft:air if block 155 152 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b39 ftm_brick 2
