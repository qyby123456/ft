scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 676 91 117 minecraft:air if block 676 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 676 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 693 91 108 minecraft:air if block 693 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 693 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 91 127 minecraft:air if block 689 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 689 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 680 91 107 minecraft:air if block 680 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 680 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 91 114 minecraft:air if block 686 92 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 686 91 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 673 91 126 minecraft:air if block 673 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 673 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 681 91 123 minecraft:air if block 681 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 681 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 692 91 119 minecraft:air if block 692 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 692 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 677 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 677 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b180 ftm_brick 2
