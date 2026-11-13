scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 182 minecraft:air if block 155 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 211 173 minecraft:air if block 166 212 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 166 211 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 211 173 minecraft:air if block 146 212 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 146 211 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 211 186 minecraft:air if block 165 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 165 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 211 186 minecraft:air if block 147 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 147 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 211 172 minecraft:air if block 156 212 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 156 211 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 211 178 minecraft:air if block 161 212 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 161 211 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 211 179 minecraft:air if block 149 212 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 149 211 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 211 182 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 211 182 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b15 ftm_brick 2
