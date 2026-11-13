scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 211 177 minecraft:air if block 150 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 150 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 211 188 minecraft:air if block 165 212 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 165 211 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 211 175 minecraft:air if block 163 212 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 163 211 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 211 188 minecraft:air if block 147 212 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 147 211 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 211 182 minecraft:air if block 158 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 158 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 184 minecraft:air if block 151 212 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 174 minecraft:air if block 155 212 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 188 minecraft:air if block 155 212 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b15 ftm_brick 2
