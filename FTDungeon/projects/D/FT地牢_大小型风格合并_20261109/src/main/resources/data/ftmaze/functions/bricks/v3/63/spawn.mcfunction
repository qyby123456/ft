scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 91 175 minecraft:air if block 150 92 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 150 91 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 186 minecraft:air if block 166 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 186 minecraft:air if block 146 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 91 173 minecraft:air if block 163 92 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 163 91 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 91 179 minecraft:air if block 158 92 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 158 91 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 91 182 minecraft:air if block 151 92 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 151 91 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 172 minecraft:air if block 155 92 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 91 179 minecraft:air if block 147 92 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 147 91 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 176 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 176 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b63 ftm_brick 2
