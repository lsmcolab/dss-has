# Real-time Learning and Planning in Environments with Swarms:A Hierarchical and a Parameter-based Simulation Approach[1]

## Introduction
Project extends Craig Reynolds boid model in application of infiltration game where
one attacker tries to get to the target which is defended by patrolling boids.
In the codebase user can try to run it using DSS approach which is Monte Carlo Simulations
as well as Hierarchical approach using ARIMA model to fit ellipse over the swarm.
Naive is our baseline algorithm to compare to our DSS and HAS approaches. It is attraction vector always
going in a straight line to the target and getting repulsive force from defenders if attacker happens to be too close to them. 

## Running the simulation
In order to run the code Adaptive Flock.jar needs to included as a module
of the project as well as args parameters needs to be passed

```<staring X pos> <starting Y pos> <id of run> <difficulty(hard/medium/easy)> <no. of defenders> <output path>```

## Directories
From `DSS` folder you can run Monte Carlo Simulation approach  
From `HASS` folder you can run ARIMA model  
From `Naive` folder you can see our baseline algorithm

## References
[1]L.Pelcner, S.Li, M.Alves, L.Marcolino, and A.Collins. 2020. Real-time Learning and Planning in Environments with Swarms:A Hierarchical and a Parameter-based Simulation Approach. In Proceedings of the xth International Conference onAutonomous Agents and Multi-agents Systems (AAMAS’2020)