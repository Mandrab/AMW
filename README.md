<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed systems</i>'s exam project.</br>
In this application some agents would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to acquire some procedural knowledge from a remote repository of abilities.</br></br>
Below are present a useful list of commands to run the system.

<h2>Docker commands</h2>
This paragraph contains a list of commands used to run the demo via gradle and docker.<br/><br/>

Initialization of the system:
0. `./gradlew download_file` download auxiliary files
1. `./gradlew unzip` unzip auxiliary files

To run the system: `./gradlew start_system [-Padmin]` to start main container, agents and user interface 
(or admin if flag is provided)

Other useful commands:
- Docker:
    - `docker images` see all the installed images
    - `docker run IMAGE` run the agent from its image
    - `docker logs CONTAINER` print the container logs on CLI
    - ``docker stop `docker containers ls -aq` `` stop ***ALL*** the container in your OS!
    - ``docker container rm -f `docker container ls -aq` `` remove ***ALL*** the container in your OS!
    - ``docker rmi -f `docker images -aq` `` remove ***ALL*** the images in your OS!
- JADE:
    - `java -cp path/to/JADE_JAR jade.Boot -gui` start JADE main container from CLI
    - `java -cp path/to/JADE_&_JASON_JAR jade.Boot -container "AGENT_NAME:jason.infra.jade.JadeAgArch(MAIN_ASL_FILE_NAME)"` start an asl agent by CLI
