scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 717 91 198 minecraft:air if block 717 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 717 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 91 202 minecraft:air if block 707 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 707 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 722 91 202 minecraft:air if block 722 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 722 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 710 91 198 minecraft:air if block 710 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 710 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 714 91 202 minecraft:air if block 714 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 714 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 711 91 201 minecraft:air if block 711 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 711 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 91 199 minecraft:air if block 713 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 713 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 718 91 201 minecraft:air if block 718 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 718 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 718 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 718 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b364 ftm_brick 2
