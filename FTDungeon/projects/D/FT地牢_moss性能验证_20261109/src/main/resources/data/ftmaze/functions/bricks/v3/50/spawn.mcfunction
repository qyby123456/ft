scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 77 91 149 minecraft:air if block 77 92 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 77 91 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 91 156 minecraft:air if block 63 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 63 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 146 minecraft:air if block 66 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 91 158 minecraft:air if block 81 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 81 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 152 minecraft:air if block 70 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 73 91 144 minecraft:air if block 73 92 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 73 91 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 91 158 minecraft:air if block 72 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 72 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 75 91 154 minecraft:air if block 75 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 75 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 78 91 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 78 91 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b50 ftm_brick 2
