scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 676 91 72 minecraft:air if block 676 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 676 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 693 91 86 minecraft:air if block 693 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 693 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 691 91 66 minecraft:air if block 691 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 691 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 673 91 86 minecraft:air if block 673 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 673 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 683 91 80 minecraft:air if block 683 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 683 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 91 73 minecraft:air if block 687 92 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 687 91 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 680 91 66 minecraft:air if block 680 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 680 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 691 91 78 minecraft:air if block 691 92 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 691 91 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 677 91 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 677 91 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b183 ftm_brick 2
