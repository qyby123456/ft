scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 31 150 minecraft:air if block 22 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 22 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 31 157 minecraft:air if block 38 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 38 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 31 145 minecraft:air if block 33 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 33 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 31 158 minecraft:air if block 26 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 26 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 156 minecraft:air if block 18 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 31 152 minecraft:air if block 30 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 30 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 145 minecraft:air if block 18 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 31 146 minecraft:air if block 26 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 26 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 31 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 31 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b149 ftm_brick 2
