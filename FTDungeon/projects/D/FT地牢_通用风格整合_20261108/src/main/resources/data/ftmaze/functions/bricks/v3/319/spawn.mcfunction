scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 666 91 198 minecraft:air if block 666 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 666 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 91 202 minecraft:air if block 656 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 656 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 91 202 minecraft:air if block 671 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 671 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 659 91 198 minecraft:air if block 659 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 659 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 663 91 202 minecraft:air if block 663 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 663 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 660 91 201 minecraft:air if block 660 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 660 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 662 91 199 minecraft:air if block 662 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 662 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 667 91 201 minecraft:air if block 667 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 667 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 667 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 667 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b319 ftm_brick 2
