scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 31 102 minecraft:air if block 22 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 22 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 31 109 minecraft:air if block 38 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 38 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 31 97 minecraft:air if block 33 32 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 33 31 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 31 110 minecraft:air if block 26 32 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 26 31 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 108 minecraft:air if block 18 32 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 30 31 104 minecraft:air if block 30 32 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 30 31 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 18 31 97 minecraft:air if block 18 32 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 18 31 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 26 31 98 minecraft:air if block 26 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 26 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 31 101 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 31 101 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b148 ftm_brick 2
