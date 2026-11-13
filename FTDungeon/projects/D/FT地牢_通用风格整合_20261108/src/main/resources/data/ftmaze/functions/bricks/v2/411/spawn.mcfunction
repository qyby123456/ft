scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1689 31 198 minecraft:air if block 1689 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1689 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1679 31 202 minecraft:air if block 1679 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1679 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1694 31 202 minecraft:air if block 1694 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1694 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1682 31 198 minecraft:air if block 1682 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1682 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1686 31 202 minecraft:air if block 1686 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1686 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1683 31 201 minecraft:air if block 1683 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1683 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1685 31 199 minecraft:air if block 1685 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1685 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1690 31 201 minecraft:air if block 1690 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1690 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1690 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1690 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b411 ftm_brick 2
