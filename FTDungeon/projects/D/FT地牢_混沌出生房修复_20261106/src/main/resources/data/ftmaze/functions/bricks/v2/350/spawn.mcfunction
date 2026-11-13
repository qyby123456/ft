scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 853 91 198 minecraft:air if block 853 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 853 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 91 202 minecraft:air if block 843 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 843 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 858 91 202 minecraft:air if block 858 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 858 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 91 198 minecraft:air if block 846 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 846 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 91 202 minecraft:air if block 850 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 91 201 minecraft:air if block 847 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 847 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 91 199 minecraft:air if block 849 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 849 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 854 91 201 minecraft:air if block 854 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 854 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 854 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 854 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b350 ftm_brick 2
