scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 25 92 204 minecraft:air if block 25 93 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 25 92 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 92 193 minecraft:air if block 37 93 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 92 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 92 193 minecraft:air if block 19 93 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 92 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 92 208 minecraft:air if block 36 93 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 36 92 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 92 195 minecraft:air if block 28 93 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 28 92 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 92 208 minecraft:air if block 19 93 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 92 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 92 201 minecraft:air if block 31 93 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 31 92 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 20 92 200 minecraft:air if block 20 93 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 20 92 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 26 92 204 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 26 92 204 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b147 ftm_brick 2
