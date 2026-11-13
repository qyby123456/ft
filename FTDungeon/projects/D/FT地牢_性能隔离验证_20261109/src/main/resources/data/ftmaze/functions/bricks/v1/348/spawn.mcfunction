scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 643 31 198 minecraft:air if block 643 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 643 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 633 31 202 minecraft:air if block 633 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 633 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 31 202 minecraft:air if block 648 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 648 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 636 31 198 minecraft:air if block 636 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 636 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 31 202 minecraft:air if block 640 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 640 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 637 31 201 minecraft:air if block 637 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 637 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 639 31 199 minecraft:air if block 639 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 639 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 644 31 201 minecraft:air if block 644 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 644 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 644 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 644 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b348 ftm_brick 2
