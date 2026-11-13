scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 843 31 198 minecraft:air if block 843 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 843 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 833 31 202 minecraft:air if block 833 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 833 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 31 202 minecraft:air if block 848 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 848 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 836 31 198 minecraft:air if block 836 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 836 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 31 202 minecraft:air if block 840 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 840 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 31 201 minecraft:air if block 837 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 837 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 31 199 minecraft:air if block 839 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 839 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 844 31 201 minecraft:air if block 844 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 844 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 844 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 844 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b363 ftm_brick 2
