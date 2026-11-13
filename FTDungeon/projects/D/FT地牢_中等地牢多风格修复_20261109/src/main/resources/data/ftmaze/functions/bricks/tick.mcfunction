scoreboard players add #brickclock ftm_state 1
execute if score #brickclock ftm_state matches 5.. run execute if score #layout ftm_state matches 1 run function ftmaze:bricks/v0/tick
execute if score #brickclock ftm_state matches 5.. run execute if score #layout ftm_state matches 2 run function ftmaze:bricks/v1/tick
execute if score #brickclock ftm_state matches 5.. run execute if score #layout ftm_state matches 3 run function ftmaze:bricks/v2/tick
execute if score #brickclock ftm_state matches 5.. run execute if score #layout ftm_state matches 4 run function ftmaze:bricks/v3/tick
execute if score #brickclock ftm_state matches 5.. run scoreboard players set #brickclock ftm_state 0
