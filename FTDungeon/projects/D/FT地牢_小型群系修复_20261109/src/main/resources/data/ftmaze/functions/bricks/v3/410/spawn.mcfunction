scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1731 151 198 minecraft:air if block 1731 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1731 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1721 151 202 minecraft:air if block 1721 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1721 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1736 151 202 minecraft:air if block 1736 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1736 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1724 151 198 minecraft:air if block 1724 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1724 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1728 151 202 minecraft:air if block 1728 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1728 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1725 151 201 minecraft:air if block 1725 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1725 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1727 151 199 minecraft:air if block 1727 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1727 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1732 151 201 minecraft:air if block 1732 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1732 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1732 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1732 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b410 ftm_brick 2
