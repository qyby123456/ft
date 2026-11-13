scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 91 176 minecraft:air if block 93 92 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 93 91 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 91 186 minecraft:air if block 109 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 109 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 91 175 minecraft:air if block 106 92 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 106 91 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 91 186 minecraft:air if block 96 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 96 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 91 180 minecraft:air if block 100 92 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 100 91 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 91 183 minecraft:air if block 91 92 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 91 91 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 91 174 minecraft:air if block 99 92 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 99 91 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 91 185 minecraft:air if block 102 92 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 102 91 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 91 176 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 91 176 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b55 ftm_brick 2
