<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed systems</i>'s exam project.</br>
In this application some agent would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to auto-acquire some (procedural) knowledge from a remote repository of abilities.</br></br>
Below are present a usefull list of commands to run the system.

<h2>Docker commands</h2>
This paragraph contains a list of commands used to run the demo via gradle and docker.<br/><br/>

Initialization of the system:
0. `./gradlew docker` to create the images of the agents
1. `docker network create amw` to create the virtual network of the system

To run the system:
0. `./gradlew start_main_container` to start the JADE main container
1. `./gradlew dockerRun` to start all the agents of the system

Other useful commands:
- `docker images` see all the installed images
- `docker run --network amw IMAGE` run the agent on the amw virtual network
- `docker rmi -f (docker images -a -q)` **WARNING** remove ***ALL*** the images in your OS!