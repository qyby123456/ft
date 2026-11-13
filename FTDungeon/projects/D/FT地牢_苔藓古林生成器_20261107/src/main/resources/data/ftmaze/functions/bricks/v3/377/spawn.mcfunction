scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 909 91 198 minecraft:air if block 909 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 909 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 91 202 minecraft:air if block 899 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 899 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 91 202 minecraft:air if block 914 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 914 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 902 91 198 minecraft:air if block 902 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 902 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 906 91 202 minecraft:air if block 906 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 906 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 903 91 201 minecraft:air if block 903 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 903 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 905 91 199 minecraft:air if block 905 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 905 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 910 91 201 minecraft:air if block 910 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 910 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 910 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 910 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b377 ftm_brick 2
