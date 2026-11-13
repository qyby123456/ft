scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 126 minecraft:air if block 155 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 117 minecraft:air if block 166 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 117 minecraft:air if block 146 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 91 130 minecraft:air if block 165 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 165 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 91 130 minecraft:air if block 147 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 147 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 91 116 minecraft:air if block 156 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 156 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 91 122 minecraft:air if block 161 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 161 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 91 123 minecraft:air if block 149 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 149 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 91 126 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 91 126 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b61 ftm_brick 2
