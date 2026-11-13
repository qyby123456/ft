scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 121 minecraft:air if block 206 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 91 132 minecraft:air if block 221 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 221 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 119 minecraft:air if block 219 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 91 132 minecraft:air if block 203 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 203 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 91 126 minecraft:air if block 214 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 214 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 128 minecraft:air if block 207 92 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 118 minecraft:air if block 211 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 132 minecraft:air if block 211 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b69 ftm_brick 2
