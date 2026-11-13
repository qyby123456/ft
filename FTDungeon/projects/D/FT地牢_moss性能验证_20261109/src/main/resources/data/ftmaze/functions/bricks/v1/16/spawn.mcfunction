scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 211 91 minecraft:air if block 178 212 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 178 211 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 102 minecraft:air if block 194 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 102 minecraft:air if block 174 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 211 89 minecraft:air if block 191 212 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 191 211 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 211 95 minecraft:air if block 186 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 186 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 98 minecraft:air if block 179 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 211 88 minecraft:air if block 183 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 183 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 211 95 minecraft:air if block 175 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 175 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 92 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 92 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b16 ftm_brick 2
