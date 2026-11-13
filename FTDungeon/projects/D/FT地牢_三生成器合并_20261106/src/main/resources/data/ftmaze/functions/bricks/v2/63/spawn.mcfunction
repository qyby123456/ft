scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 91 177 minecraft:air if block 151 92 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 151 91 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 91 187 minecraft:air if block 162 92 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 162 91 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 173 minecraft:air if block 166 92 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 91 187 minecraft:air if block 149 92 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 149 91 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 183 minecraft:air if block 155 92 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 91 172 minecraft:air if block 156 92 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 156 91 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 91 178 minecraft:air if block 160 92 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 160 91 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 173 minecraft:air if block 146 92 173 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 173 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b63 ftm_brick 2
