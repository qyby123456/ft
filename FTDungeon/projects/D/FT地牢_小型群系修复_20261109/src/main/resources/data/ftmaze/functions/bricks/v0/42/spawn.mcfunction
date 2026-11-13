scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 151 151 minecraft:air if block 178 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 178 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 151 158 minecraft:air if block 194 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 194 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 151 146 minecraft:air if block 189 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 189 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 159 minecraft:air if block 182 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 157 minecraft:air if block 174 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 151 153 minecraft:air if block 186 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 186 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 146 minecraft:air if block 174 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 147 minecraft:air if block 182 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b42 ftm_brick 2
