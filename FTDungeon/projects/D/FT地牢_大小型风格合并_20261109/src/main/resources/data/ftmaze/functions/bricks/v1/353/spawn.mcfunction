scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 853 31 198 minecraft:air if block 853 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 853 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 31 202 minecraft:air if block 843 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 843 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 858 31 202 minecraft:air if block 858 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 858 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 31 198 minecraft:air if block 846 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 846 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 31 202 minecraft:air if block 850 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 850 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 31 201 minecraft:air if block 847 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 847 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 31 199 minecraft:air if block 849 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 849 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 854 31 201 minecraft:air if block 854 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 854 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 854 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 854 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b353 ftm_brick 2
