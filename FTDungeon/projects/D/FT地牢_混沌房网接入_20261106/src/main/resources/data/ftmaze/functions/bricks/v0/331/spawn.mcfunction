scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 666 31 198 minecraft:air if block 666 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 666 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 31 202 minecraft:air if block 656 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 31 202 minecraft:air if block 671 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 671 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 659 31 198 minecraft:air if block 659 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 659 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 663 31 202 minecraft:air if block 663 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 663 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 660 31 201 minecraft:air if block 660 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 660 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 662 31 199 minecraft:air if block 662 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 662 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 667 31 201 minecraft:air if block 667 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 667 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 667 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 667 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b331 ftm_brick 2
