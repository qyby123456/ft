scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 149 minecraft:air if block 179 212 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 159 minecraft:air if block 190 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 145 minecraft:air if block 194 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 211 159 minecraft:air if block 177 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 177 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 211 155 minecraft:air if block 183 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 183 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 211 144 minecraft:air if block 184 212 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 184 211 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 211 150 minecraft:air if block 188 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 188 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 145 minecraft:air if block 174 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b18 ftm_brick 2
