scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 148 minecraft:air if block 22 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 91 158 minecraft:air if block 35 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 35 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 91 145 minecraft:air if block 37 92 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 91 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 91 156 minecraft:air if block 19 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 91 151 minecraft:air if block 30 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 30 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 144 minecraft:air if block 27 92 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 24 91 153 minecraft:air if block 24 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 24 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 91 152 minecraft:air if block 36 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 36 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b146 ftm_brick 2
