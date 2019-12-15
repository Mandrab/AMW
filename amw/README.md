<h1>AMW - Agents-Managed Warehouse</h1>

**AMW** project represent the implementation of the <i>distributed system</i>'s exam project.</br>
In this project some agent would cooperate to achieve the goal of manage a warehouse. Furthermore, we want the agents to be able to auto-acquire some (procedural) knowledge from a remote repository of abilities.</br></br>
Below are present a usefull list of commands to run the system.

<h2>Commands</h2>
This paragraph contains a list of commands used to run the demo via gradle. Atm, only a command is present but i expect more in future.<br/><br/>

`./gradlew config` setup the Jason's config file</br>
`./gradlew run_system` run the .mas2j (Jason) project over JADE</br> 
`./gradlew run_terminal` run the human interface to the system (the terminal). Require a JADE environment yet running</br>
`./gradlew run [-Pall]` run the .mas2j (Jason) project over JADE in detached mode. Specifing <i>-Pall</i> flag, also the terminal will start.</br>