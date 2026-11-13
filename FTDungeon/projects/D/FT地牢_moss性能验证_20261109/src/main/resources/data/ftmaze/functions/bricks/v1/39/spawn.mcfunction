scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 182 minecraft:air if block 155 152 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 151 173 minecraft:air if block 166 152 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 166 151 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 151 173 minecraft:air if block 146 152 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 146 151 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 151 186 minecraft:air if block 165 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 165 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 151 186 minecraft:air if block 147 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 147 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 151 172 minecraft:air if block 156 152 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 156 151 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 151 178 minecraft:air if block 161 152 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 161 151 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 151 179 minecraft:air if block 149 152 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 149 151 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 151 182 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 151 182 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b39 ftm_brick 2
