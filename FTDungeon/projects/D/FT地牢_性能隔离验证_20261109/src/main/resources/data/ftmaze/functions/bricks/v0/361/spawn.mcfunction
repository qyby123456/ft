scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 91 198 minecraft:air if block 835 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 835 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 825 91 202 minecraft:air if block 825 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 825 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 91 202 minecraft:air if block 840 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 840 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 828 91 198 minecraft:air if block 828 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 828 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 832 91 202 minecraft:air if block 832 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 832 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 829 91 201 minecraft:air if block 829 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 829 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 831 91 199 minecraft:air if block 831 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 831 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 836 91 201 minecraft:air if block 836 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 836 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 836 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 836 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b361 ftm_brick 2
