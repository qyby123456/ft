scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 667 91 198 minecraft:air if block 667 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 667 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 657 91 202 minecraft:air if block 657 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 657 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 672 91 202 minecraft:air if block 672 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 672 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 660 91 198 minecraft:air if block 660 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 660 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 664 91 202 minecraft:air if block 664 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 664 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 661 91 201 minecraft:air if block 661 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 661 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 663 91 199 minecraft:air if block 663 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 663 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 668 91 201 minecraft:air if block 668 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 668 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 668 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 668 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b357 ftm_brick 2
