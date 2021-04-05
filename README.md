<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed systems</i>'s exam project.</br>
In this application some agent would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to auto-acquire some (procedural) knowledge from a remote repository of abilities.</br></br>
Below are present a useful list of commands to run the system.

<h2>Docker commands</h2>
This paragraph contains a list of commands used to run the demo via gradle and docker.<br/><br/>

Initialization of the system:
0. `./gradlew downloadFile` download auxiliary files
1. `./gradlew unzip` unzip auxiliary files
2. `./gradlew [warehouse:AGENT_TYPE:]docker` creates the image of the agent(s) (they should not already exist)
3. `docker network create amw` create the virtual network of the system

To run the system:
0. `./gradlew start_main_container` to start the JADE main container
1. `./gradlew [warehouse:AGENT_TYPE:]dockerRun` to start the agent(s) of the system

Other useful commands:
- Gradle:
    - `./gradlew start_agent [-Pp=PATH/TO] -Pf=MAIN_FILE.asl [-Pn=AGENT_NAME]` start and *asl* agent. Needs a main JADE container running.
- Docker:
    - `docker images` see all the installed images
    - `docker run --network amw IMAGE` run the agent on the amw virtual network
    - `docker logs CONTAINER` print the container CLI
    - ``docker stop -f `docker images -aq` `` **WARNING** stop ***ALL*** the container in your OS!
    - ``docker rm -f `docker images -aq` `` **WARNING** remove ***ALL*** the container in your OS!
    - ``docker rmi -f `docker images -aq` `` **WARNING** remove ***ALL*** the images in your OS!
- JADE:
    - `java -cp path/to/JADE_JAR jade.Boot -gui` start JADE main container from CLI
    - `java -cp path/to/JADE_&_JASON_JAR jade.Boot -container "agent2:jason.infra.jade.JadeAgArch(ASL_AGENT_NAME)"` start an asl agent by CLI
