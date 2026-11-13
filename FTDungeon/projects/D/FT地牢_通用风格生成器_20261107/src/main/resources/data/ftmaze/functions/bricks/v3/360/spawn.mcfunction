scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 717 31 198 minecraft:air if block 717 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 717 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 31 202 minecraft:air if block 707 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 707 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 722 31 202 minecraft:air if block 722 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 722 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 710 31 198 minecraft:air if block 710 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 710 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 714 31 202 minecraft:air if block 714 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 714 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 711 31 201 minecraft:air if block 711 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 711 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 31 199 minecraft:air if block 713 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 713 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 718 31 201 minecraft:air if block 718 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 718 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 718 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 718 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b360 ftm_brick 2
