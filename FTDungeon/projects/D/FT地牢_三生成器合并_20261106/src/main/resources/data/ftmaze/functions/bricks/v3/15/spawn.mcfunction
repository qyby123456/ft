scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 177 minecraft:air if block 151 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 211 187 minecraft:air if block 162 212 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 162 211 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 211 173 minecraft:air if block 166 212 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 166 211 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 211 187 minecraft:air if block 149 212 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 149 211 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 183 minecraft:air if block 155 212 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 211 172 minecraft:air if block 156 212 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 156 211 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 211 178 minecraft:air if block 160 212 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 160 211 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 211 173 minecraft:air if block 146 212 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 146 211 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b15 ftm_brick 2
