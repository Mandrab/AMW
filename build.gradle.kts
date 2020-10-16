//**********************************************************************************************************************
//  CONSTANT
//**********************************************************************************************************************

val mainPath = "src" + File.separator + "main" + File.separator
val mainMas = mainPath + "AMW.mas2j"
val jasonConfigurationsPath = System.getProperty("user.home") + File.separator + ".jason"

//**********************************************************************************************************************
//  MISCELLANEOUS
//**********************************************************************************************************************

///////////////////////////// PROJECT INFO

group = "dev.mandrab.uni.magistrale.sistemidistribuiti.amw"                         // identify the product of the build
version = "1.0-SNAPSHOT"                                                            // version

java { // TODO
    sourceCompatibility = JavaVersion.toVersion("14")
    targetCompatibility = JavaVersion.toVersion("14")
}

///////////////////////////// KOTLIN PLUGINS

plugins {
    kotlin("jvm") version "1.3.72"
}

//**********************************************************************************************************************
//  DEPENDENCIES
//**********************************************************************************************************************

repositories {
    mavenCentral()
    maven("http://jacamo.sourceforge.net/maven2/")                                // jason repo
    maven("https://jade.tilab.com/maven/")                                        // jade repo
}

///////////////////////////// CONFIGURATION OF MAVEN DEPENDENCIES

val junit: Configuration by configurations.creating                                 // for tests
val jason: Configuration by configurations.creating                                 // project pillar
val guava: Configuration by configurations.creating                                 // Configurator
val apacheCommonsIo: Configuration by configurations.creating                       // Configurator
val javaMailApi: Configuration by configurations.creating                           // mail sender
val snakeYaml: Configuration by configurations.creating                             // jaml parser
val corutine: Configuration by configurations.creating                              // kotlin corutines
val rxKotlin: Configuration by configurations.creating                              // reactveX for kotlin
val googleAPI: Configuration by configurations.creating


val dependencies: List<Configuration> = listOf(junit, jason, guava, apacheCommonsIo,
    javaMailApi, snakeYaml, corutine, rxKotlin, googleAPI)                          // used to setup libraries classpath

dependencies {
    junit("junit", "junit", "4.12")
    implementation(junit)
    jason("org.jason-lang", "jason", "2.4")
    implementation(jason)
    guava("com.google.guava", "guava", "28.1-jre")
    implementation(guava)
    apacheCommonsIo("commons-io", "commons-io", "2.6")
    implementation(apacheCommonsIo)
    javaMailApi("com.sun.mail", "javax.mail", "1.6.2")
    implementation(javaMailApi)
    snakeYaml("org.yaml", "snakeyaml", "1.21")
    implementation(snakeYaml)
    corutine("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.5")
    implementation(corutine)
    rxKotlin("io.reactivex.rxjava3", "rxkotlin", "3.0.0")
    implementation(rxKotlin)
    googleAPI("com.google.apis", "google-api-services-gmail", "v1-rev110-1.25.0")
    implementation(googleAPI)
}

//**********************************************************************************************************************
//  TASKS
//**********************************************************************************************************************

///////////////////////////// SETUP JASON'S CONFIG FILE

task<JavaExec>("config") {
    val antPath = jason.asPath.split(":").firstOrNull { s -> s.contains(".*/ant-.*".toRegex()) }
    val jadePath = jason.asPath.split(":").firstOrNull { s -> s.contains(".*/jade-.*".toRegex()) }

    group = "jason"
    sourceSets.main {                                                              // set cli path in which call the cmd
        classpath = runtimeClasspath
    }
    standardInput = System.`in`                                                     // redirect std input (terminal)

    main = "Configurator"

    args(antPath.orEmpty())
    args(jadePath.orEmpty())
    args(jasonConfigurationsPath)
}

///////////////////////////// SETUP LIBRARIES' CLASSPATH IN MAS2J FILE

tasks.register<Task>("setup_mas2j") {
    setupLibraryClasspath(mainMas)                        // set all the dependencies path in mas2j file
    //setupLibraryClasspath(MAS2J_FILE_NAME_TEST)                   // set all the dependencies path in mas2j test file
}

///////////////////////////// RESET LIBRARIES' CLASSPATH IN MAS2J FILE

tasks.register<Task>("reset_mas2j") {
    setupLibraryClasspath(mainMas, cleanClasspath = true) // reset dependencies patha in mas2j file
    //setupLibraryClasspath(MAS2J_FILE_NAME_TEST, cleanClasspath = true) // reset dependencies paths in mas2j test file
}

///////////////////////////// RUN MAS2J PROJECT

task<JavaExec>("run_system") {
    dependsOn("setup_mas2j")                                      // add dependencies path to mas2j

    group = "jason"                                                 // process (TODO ?) group name
    sourceSets {                                                    // set cli path in which call the cmd
        main {
            classpath = runtimeClasspath
        }
    }

    standardInput = System.`in`                                     // redirect std input (terminal)

    dependsOn("config")

    main = "jason.runtime.RunJasonProject"                          // jason mas2j runner
    args(mainMas)                                         // specify project file
}

///////////////////////////// RUN CLIENT / ADMIN TERMINAL INTERFACE

task<JavaExec>("run_terminal") {
    group = "jade-terminal"                                         // process (?) group name
    sourceSets {                                                    // set cli path in which call the cmd
        main {
            classpath = runtimeClasspath
        }
    }
    standardInput = System.`in`                                     // std input (terminal)

    main = "MainKt"                                                 // jason mas2j runner

    args("retry")                                             // keep retry connection if fails
}

///////////////////////////// RUN MAS2J PROJECT IN DETACHED MODE (-Pall RUN ALSO THE TERMINAL INTERFACE)

tasks.register<Task>("run") {
    // runs system in detached mode
    doFirst {
        ProcessBuilder()
                .directory(projectDir)
                .command("." + File.separator + "gradlew", "run_system")
                .start()
    }

    // after the system startup, run the terminal
    if (project.hasProperty("all"))
        finalizedBy("run_terminal")
}

//tasks.findByName("test")!!.dependsOn("run")
tasks.findByName("test")!!.dependsOn("config").dependsOn("setup_mas2j")

//**********************************************************************************************************************
//  FUNCTIONS
//**********************************************************************************************************************

// add dependencies classpath to mas2j project file
fun setupLibraryClasspath(filePath: String, cleanClasspath: Boolean = false, excluded: List<Regex>? = null) {
    // read the actual mas2j project file
    val fileStr = File(filePath).readLines()

    // generate the new classpath
    val strBuilder = StringBuilder()

    dependencies.forEach { conf -> conf.asPath                      // get all the path to the libraries
            .split (":")
            .filter { path -> excluded == null                      // if there isn't excluded paths, then do nothing
                    || excluded.any { path.contains(it) }           // else, check if i should exclude the path
            }.forEach { strBuilder.append("\"$it\";\n") }           // add the path to classpath
    }

    val output = emptyList<String>().toMutableList()
    val newLineIterator = strBuilder.lineSequence().iterator()
    var skip = false

    fileStr.forEach {
        if ("(\t|\n| )*classpath:(\t| |\n)*".toRegex().matches(it)) {
            output.add(it)
            skip = true
        } else if ("(\t| )*aslSourcePath:(\t| |\n)*".toRegex().matches(it)) {
            while (!cleanClasspath && newLineIterator.hasNext())
                output.add("\t\t" + newLineIterator.next())
            if (cleanClasspath || strBuilder.isEmpty())
                output.add("")
            skip = false
        }

        if (!skip) output.add(it)
    }

    // re-write the updated mas2j project file
    File(filePath).writeText(output.joinToString("\n"))
}