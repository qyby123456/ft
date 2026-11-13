scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 675 31 198 minecraft:air if block 675 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 675 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 665 31 202 minecraft:air if block 665 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 665 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 680 31 202 minecraft:air if block 680 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 680 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 668 31 198 minecraft:air if block 668 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 668 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 672 31 202 minecraft:air if block 672 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 672 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 669 31 201 minecraft:air if block 669 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 669 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 31 199 minecraft:air if block 671 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 671 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 676 31 201 minecraft:air if block 676 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 676 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 676 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 676 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b359 ftm_brick 2
