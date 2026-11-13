scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 918 31 198 minecraft:air if block 918 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 918 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 908 31 202 minecraft:air if block 908 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 908 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 31 202 minecraft:air if block 923 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 923 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 911 31 198 minecraft:air if block 911 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 911 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 915 31 202 minecraft:air if block 915 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 915 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 912 31 201 minecraft:air if block 912 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 912 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 31 199 minecraft:air if block 914 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 914 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 919 31 201 minecraft:air if block 919 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 919 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 919 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 919 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b337 ftm_brick 2
