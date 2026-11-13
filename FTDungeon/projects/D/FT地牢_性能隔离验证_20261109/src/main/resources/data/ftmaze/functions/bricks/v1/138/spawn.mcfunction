scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 196 91 60 minecraft:air if block 196 92 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 196 91 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 91 74 minecraft:air if block 208 92 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 208 91 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 72 minecraft:air if block 190 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 61 minecraft:air if block 210 92 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 91 68 minecraft:air if block 201 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 201 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 63 minecraft:air if block 190 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 197 91 74 minecraft:air if block 197 92 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 197 91 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 91 58 minecraft:air if block 203 92 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 91 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 197 91 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 197 91 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b138 ftm_brick 2
