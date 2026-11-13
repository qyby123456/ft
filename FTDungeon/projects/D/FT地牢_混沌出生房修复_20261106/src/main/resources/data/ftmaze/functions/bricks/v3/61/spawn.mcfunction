scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 91 123 minecraft:air if block 150 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 150 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 130 minecraft:air if block 166 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 91 118 minecraft:air if block 161 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 161 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 91 131 minecraft:air if block 154 92 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 154 91 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 129 minecraft:air if block 146 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 91 125 minecraft:air if block 158 92 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 158 91 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 118 minecraft:air if block 146 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 91 119 minecraft:air if block 154 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 154 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b61 ftm_brick 2
