scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 769 91 198 minecraft:air if block 769 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 769 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 759 91 202 minecraft:air if block 759 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 759 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 774 91 202 minecraft:air if block 774 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 774 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 762 91 198 minecraft:air if block 762 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 762 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 766 91 202 minecraft:air if block 766 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 766 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 763 91 201 minecraft:air if block 763 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 763 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 765 91 199 minecraft:air if block 765 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 765 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 770 91 201 minecraft:air if block 770 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 770 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 770 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 770 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b348 ftm_brick 2
