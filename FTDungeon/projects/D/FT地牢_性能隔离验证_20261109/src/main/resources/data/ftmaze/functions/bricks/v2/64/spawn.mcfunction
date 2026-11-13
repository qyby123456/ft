scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 98 minecraft:air if block 183 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 89 minecraft:air if block 194 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 89 minecraft:air if block 174 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 102 minecraft:air if block 193 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 91 102 minecraft:air if block 175 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 175 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 88 minecraft:air if block 184 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 91 94 minecraft:air if block 189 92 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 189 91 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 91 95 minecraft:air if block 177 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 177 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 91 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 91 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b64 ftm_brick 2
