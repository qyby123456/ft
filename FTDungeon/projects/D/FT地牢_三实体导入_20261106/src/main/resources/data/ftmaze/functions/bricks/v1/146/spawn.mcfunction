scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 25 92 156 minecraft:air if block 25 93 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 25 92 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 92 145 minecraft:air if block 37 93 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 92 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 92 145 minecraft:air if block 19 93 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 92 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 92 160 minecraft:air if block 36 93 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 36 92 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 92 147 minecraft:air if block 28 93 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 28 92 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 92 160 minecraft:air if block 19 93 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 19 92 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 92 153 minecraft:air if block 31 93 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 31 92 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 20 92 152 minecraft:air if block 20 93 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 20 92 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 26 92 156 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 26 92 156 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b146 ftm_brick 2
