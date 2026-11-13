scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1689 151 198 minecraft:air if block 1689 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1689 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1679 151 202 minecraft:air if block 1679 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1679 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1694 151 202 minecraft:air if block 1694 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1694 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1682 151 198 minecraft:air if block 1682 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1682 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1686 151 202 minecraft:air if block 1686 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1686 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1683 151 201 minecraft:air if block 1683 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1683 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1685 151 199 minecraft:air if block 1685 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1685 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1690 151 201 minecraft:air if block 1690 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1690 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1690 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1690 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b409 ftm_brick 2
