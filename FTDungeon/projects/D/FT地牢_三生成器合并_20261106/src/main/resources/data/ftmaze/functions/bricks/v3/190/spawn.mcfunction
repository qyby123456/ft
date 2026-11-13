scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 883 91 115 minecraft:air if block 883 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 883 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 898 91 129 minecraft:air if block 898 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 898 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 91 109 minecraft:air if block 897 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 897 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 91 129 minecraft:air if block 880 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 880 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 91 123 minecraft:air if block 889 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 889 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 894 91 117 minecraft:air if block 894 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 894 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 887 91 109 minecraft:air if block 887 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 887 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 91 120 minecraft:air if block 879 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 879 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 883 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 883 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b190 ftm_brick 2
