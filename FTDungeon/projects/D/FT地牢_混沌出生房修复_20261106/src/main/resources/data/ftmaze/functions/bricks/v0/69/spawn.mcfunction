scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 123 minecraft:air if block 206 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 91 130 minecraft:air if block 222 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 222 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 118 minecraft:air if block 217 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 131 minecraft:air if block 210 92 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 91 129 minecraft:air if block 202 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 202 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 91 125 minecraft:air if block 214 92 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 214 91 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 91 118 minecraft:air if block 202 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 202 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 119 minecraft:air if block 210 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b69 ftm_brick 2
