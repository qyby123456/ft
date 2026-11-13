scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 98 91 186 minecraft:air if block 98 92 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 98 91 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 91 183 minecraft:air if block 109 92 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 109 91 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 194 minecraft:air if block 94 92 194 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 194 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 91 192 minecraft:air if block 103 92 192 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 103 91 192 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 91 183 minecraft:air if block 91 92 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 91 91 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 91 182 minecraft:air if block 102 92 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 102 91 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 91 191 minecraft:air if block 97 92 191 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 97 91 191 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 91 185 minecraft:air if block 105 92 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 105 91 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 99 91 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 99 91 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b55 ftm_brick 2
