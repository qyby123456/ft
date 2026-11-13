scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 919 91 198 minecraft:air if block 919 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 919 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 909 91 202 minecraft:air if block 909 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 909 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 924 91 202 minecraft:air if block 924 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 924 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 912 91 198 minecraft:air if block 912 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 912 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 916 91 202 minecraft:air if block 916 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 916 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 913 91 201 minecraft:air if block 913 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 913 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 915 91 199 minecraft:air if block 915 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 915 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 920 91 201 minecraft:air if block 920 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 920 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 920 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 920 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b363 ftm_brick 2
