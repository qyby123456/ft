scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 31 150 minecraft:air if block 66 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 66 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 31 160 minecraft:air if block 77 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 77 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 31 146 minecraft:air if block 81 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 81 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 62 31 160 minecraft:air if block 62 32 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 62 31 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 144 minecraft:air if block 71 32 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 69 31 157 minecraft:air if block 69 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 69 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 152 minecraft:air if block 74 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 31 154 minecraft:air if block 80 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 80 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 31 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 31 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b74 ftm_brick 2
