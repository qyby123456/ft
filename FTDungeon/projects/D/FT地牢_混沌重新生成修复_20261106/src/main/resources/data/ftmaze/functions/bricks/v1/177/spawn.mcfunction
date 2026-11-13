scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 643 91 162 minecraft:air if block 643 92 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 643 91 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 91 146 minecraft:air if block 658 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 658 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 146 minecraft:air if block 638 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 91 164 minecraft:air if block 658 92 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 658 91 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 151 minecraft:air if block 648 92 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 91 159 minecraft:air if block 651 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 651 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 156 minecraft:air if block 638 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 91 154 minecraft:air if block 656 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 656 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 643 91 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 643 91 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b177 ftm_brick 2
