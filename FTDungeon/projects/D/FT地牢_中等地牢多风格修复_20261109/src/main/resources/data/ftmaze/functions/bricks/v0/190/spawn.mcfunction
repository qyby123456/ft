scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 883 211 115 minecraft:air if block 883 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 883 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 898 211 129 minecraft:air if block 898 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 898 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 211 109 minecraft:air if block 897 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 897 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 211 129 minecraft:air if block 880 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 880 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 211 123 minecraft:air if block 889 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 889 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 894 211 117 minecraft:air if block 894 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 894 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 887 211 109 minecraft:air if block 887 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 887 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 211 120 minecraft:air if block 879 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 879 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 883 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 883 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b190 ftm_brick 2
