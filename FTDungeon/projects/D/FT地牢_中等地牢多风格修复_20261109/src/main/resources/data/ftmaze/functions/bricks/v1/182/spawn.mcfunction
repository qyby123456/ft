scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 643 91 76 minecraft:air if block 643 92 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 643 91 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 91 88 minecraft:air if block 658 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 658 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 657 91 68 minecraft:air if block 657 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 657 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 88 minecraft:air if block 638 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 68 minecraft:air if block 638 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 91 81 minecraft:air if block 651 92 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 651 91 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 91 85 minecraft:air if block 645 92 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 645 91 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 91 69 minecraft:air if block 646 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 646 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 91 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 91 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b182 ftm_brick 2
