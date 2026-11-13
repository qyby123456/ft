scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 167 91 206 minecraft:air if block 167 92 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 167 91 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 157 91 210 minecraft:air if block 157 92 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 157 91 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 172 91 210 minecraft:air if block 172 92 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 172 91 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 91 206 minecraft:air if block 160 92 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 160 91 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 164 91 210 minecraft:air if block 164 92 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 164 91 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 91 209 minecraft:air if block 161 92 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 161 91 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 91 207 minecraft:air if block 163 92 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 163 91 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 168 91 209 minecraft:air if block 168 92 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 168 91 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 168 91 206 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 168 91 206 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b287 ftm_brick 2
