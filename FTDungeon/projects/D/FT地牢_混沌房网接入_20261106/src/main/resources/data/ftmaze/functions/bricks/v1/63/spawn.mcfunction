scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 91 179 minecraft:air if block 150 92 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 150 91 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 186 minecraft:air if block 166 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 91 174 minecraft:air if block 161 92 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 161 91 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 91 187 minecraft:air if block 154 92 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 154 91 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 185 minecraft:air if block 146 92 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 91 181 minecraft:air if block 158 92 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 158 91 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 174 minecraft:air if block 146 92 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 91 175 minecraft:air if block 154 92 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 154 91 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b63 ftm_brick 2
