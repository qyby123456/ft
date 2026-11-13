scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 759 91 198 minecraft:air if block 759 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 759 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 749 91 202 minecraft:air if block 749 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 749 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 764 91 202 minecraft:air if block 764 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 764 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 752 91 198 minecraft:air if block 752 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 752 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 756 91 202 minecraft:air if block 756 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 756 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 753 91 201 minecraft:air if block 753 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 753 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 91 199 minecraft:air if block 755 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 755 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 760 91 201 minecraft:air if block 760 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 760 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 760 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 760 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b365 ftm_brick 2
