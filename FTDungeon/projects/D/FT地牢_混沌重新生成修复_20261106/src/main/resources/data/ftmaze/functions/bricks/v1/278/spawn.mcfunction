scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 207 minecraft:air if block 70 92 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 197 minecraft:air if block 74 92 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 212 minecraft:air if block 74 92 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 200 minecraft:air if block 70 92 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 204 minecraft:air if block 74 92 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 211 minecraft:air if block 70 92 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 91 203 minecraft:air if block 71 92 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 71 91 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 91 209 minecraft:air if block 72 92 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 72 91 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 91 208 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 91 208 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b278 ftm_brick 2
