scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 642 91 115 minecraft:air if block 642 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 642 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 91 129 minecraft:air if block 658 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 658 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 657 91 109 minecraft:air if block 657 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 657 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 129 minecraft:air if block 638 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 124 minecraft:air if block 648 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 654 91 118 minecraft:air if block 654 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 654 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 109 minecraft:air if block 638 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 647 91 110 minecraft:air if block 647 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 647 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b178 ftm_brick 2
