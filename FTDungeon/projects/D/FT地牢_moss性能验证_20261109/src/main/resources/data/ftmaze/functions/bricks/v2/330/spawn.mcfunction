scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 582 31 198 minecraft:air if block 582 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 582 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 572 31 202 minecraft:air if block 572 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 572 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 31 202 minecraft:air if block 587 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 587 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 575 31 198 minecraft:air if block 575 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 575 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 579 31 202 minecraft:air if block 579 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 579 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 576 31 201 minecraft:air if block 576 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 576 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 578 31 199 minecraft:air if block 578 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 578 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 583 31 201 minecraft:air if block 583 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 583 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 583 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 583 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b330 ftm_brick 2
