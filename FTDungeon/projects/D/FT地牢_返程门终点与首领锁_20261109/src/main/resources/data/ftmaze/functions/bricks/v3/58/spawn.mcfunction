scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 150 minecraft:air if block 122 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 91 160 minecraft:air if block 133 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 133 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 91 146 minecraft:air if block 137 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 118 91 160 minecraft:air if block 118 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 118 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 91 144 minecraft:air if block 127 92 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 127 91 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 91 157 minecraft:air if block 125 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 125 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 91 152 minecraft:air if block 130 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 130 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 91 154 minecraft:air if block 136 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b58 ftm_brick 2
