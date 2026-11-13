scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 91 91 minecraft:air if block 178 92 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 178 91 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 102 minecraft:air if block 194 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 102 minecraft:air if block 174 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 91 89 minecraft:air if block 191 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 191 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 91 95 minecraft:air if block 186 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 186 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 98 minecraft:air if block 179 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 88 minecraft:air if block 183 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 91 95 minecraft:air if block 175 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 175 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 92 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 92 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b64 ftm_brick 2
