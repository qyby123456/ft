scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 151 179 minecraft:air if block 150 152 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 150 151 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 151 186 minecraft:air if block 166 152 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 166 151 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 151 174 minecraft:air if block 161 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 161 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 151 187 minecraft:air if block 154 152 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 154 151 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 151 185 minecraft:air if block 146 152 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 146 151 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 151 181 minecraft:air if block 158 152 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 158 151 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 151 174 minecraft:air if block 146 152 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 146 151 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 151 175 minecraft:air if block 154 152 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 154 151 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b39 ftm_brick 2
