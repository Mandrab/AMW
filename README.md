<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed systems</i>'s exam project.</br>
In this application some agent would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to auto-acquire some (procedural) knowledge from a remote repository of abilities.</br></br>
Below are present a useful list of commands to run the system.

<h2>Docker commands</h2>
This paragraph contains a list of commands used to run the demo via gradle and docker.<br/><br/>

Initialization of the system:
0. `./gradlew download_file` download auxiliary files
1. `./gradlew unzip` unzip auxiliary files
2. `./gradlew create_AGENT_NAME_container | create_containers` creates the container of the agent | agents

To run the system: `./gradlew start_system` to start the main container and the agents

Other useful commands:
- Gradle complete commands list:
    - `./gradlew start_main_container` to start the JADE main container
    - `./gradlew create_AGENT_NAME_dockerfile` generate the Dockerfile for the agent
    - `./gradlew build_AGENT_NAME_image | build_images` build the image of the agent | agents
    - `./gradlew create_AGENT_NAME_container | create_containers` build the container of the agent | agents
    - `./gradlew start_AGENT_NAME_agent | start_agents` start the agent | agents. Needs JADE main container running
    - `./gradlew log_AGENT_NAME_agent | show_logs` show logs of the agent | agents in the system
    - `./gradlew remove_AGENT_NAME_container | remove_containers` remove container of the agent | agents
    - `./gradlew download_file` download auxiliary files (jason libs)
    - `./gradlew unzip` unzip auxiliary files (jason zip)
    - `./gradlew start_main_container` starts JADE main container
    - `./gradlew start_agent [-Pp=PATH/TO] -Pf=MAIN_FILE.asl [-Pn=AGENT_NAME]` start an *asl* agent. Needs JADE main container running
    - `./gradlew start_system` to start the main container and the agents. Require initialization process to be executed
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
