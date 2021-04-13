<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed systems</i>'s exam project.</br>
In this application some agent would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to auto-acquire some (procedural) knowledge from a remote repository of abilities.</br></br>
Below are present a usefull list of commands to run the system.

<h2>Commands</h2>
This paragraph contains a list of commands used to run the demo via gradle.<br/><br/>

`./gradlew config` setup the Jason's config file</br>
`./gradlew reset_mas2j` clean the classpath specified in the .mas2j  (project config) file</br>
>   <sub>Due to a locality of the gradle's cached libraries, the command is useful to git a cleaner and more correct version of the file that doesn't contains any local path. Anyway, the system automatically check the classpath at every startup, so this command is not strictly necessarely</sub>

`./gradlew run_system` run the .mas2j (Jason) project over JADE</br> 
`./gradlew run_terminal` run the human interface to the system (the control "terminal"). Require a JADE environment yet running</br>
>   <sub>At startup is usual to see throwed some Exceptions. That's caused by the "terminal" that try to connect to a JADE system that hasn't yet started. Anyway, when this last one will finally starts, the Exceptions will (hopefully) stop</sub>

`./gradlew run [-Pall]` run the .mas2j (Jason) project over JADE in detached mode. Specifing <i>-Pall</i> flag, also the terminal will start.</br>