scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1579 91 198 minecraft:air if block 1579 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1579 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1571 91 201 minecraft:air if block 1571 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1571 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1583 91 203 minecraft:air if block 1583 92 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1583 91 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1573 91 197 minecraft:air if block 1573 92 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1573 91 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1579 91 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1579 91 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b393 ftm_brick 2
