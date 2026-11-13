scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 867 91 198 minecraft:air if block 867 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 867 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 857 91 202 minecraft:air if block 857 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 857 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 91 202 minecraft:air if block 872 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 872 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 860 91 198 minecraft:air if block 860 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 860 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 864 91 202 minecraft:air if block 864 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 864 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 861 91 201 minecraft:air if block 861 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 861 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 863 91 199 minecraft:air if block 863 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 863 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 868 91 201 minecraft:air if block 868 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 868 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 868 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 868 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b376 ftm_brick 2
