scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 675 91 198 minecraft:air if block 675 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 675 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 665 91 202 minecraft:air if block 665 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 665 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 680 91 202 minecraft:air if block 680 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 680 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 668 91 198 minecraft:air if block 668 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 668 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 672 91 202 minecraft:air if block 672 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 672 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 669 91 201 minecraft:air if block 669 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 669 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 91 199 minecraft:air if block 671 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 671 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 676 91 201 minecraft:air if block 676 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 676 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 676 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 676 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b363 ftm_brick 2
