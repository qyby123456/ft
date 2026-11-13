scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 751 91 198 minecraft:air if block 751 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 751 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 741 91 202 minecraft:air if block 741 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 741 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 756 91 202 minecraft:air if block 756 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 756 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 744 91 198 minecraft:air if block 744 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 744 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 748 91 202 minecraft:air if block 748 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 748 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 745 91 201 minecraft:air if block 745 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 745 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 747 91 199 minecraft:air if block 747 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 747 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 752 91 201 minecraft:air if block 752 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 752 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 752 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 752 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b359 ftm_brick 2
