scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1731 31 198 minecraft:air if block 1731 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1731 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1721 31 202 minecraft:air if block 1721 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1721 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1736 31 202 minecraft:air if block 1736 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1736 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1724 31 198 minecraft:air if block 1724 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1724 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1728 31 202 minecraft:air if block 1728 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1728 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1725 31 201 minecraft:air if block 1725 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1725 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1727 31 199 minecraft:air if block 1727 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1727 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1732 31 201 minecraft:air if block 1732 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1732 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1732 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1732 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b410 ftm_brick 2
