scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 843 91 198 minecraft:air if block 843 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 843 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 833 91 202 minecraft:air if block 833 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 833 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 91 202 minecraft:air if block 848 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 848 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 836 91 198 minecraft:air if block 836 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 836 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 91 202 minecraft:air if block 840 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 840 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 91 201 minecraft:air if block 837 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 837 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 91 199 minecraft:air if block 839 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 839 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 844 91 201 minecraft:air if block 844 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 844 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 844 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 844 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b367 ftm_brick 2
