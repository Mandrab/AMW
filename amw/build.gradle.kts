//**********************************************************************************************************************
//  IMPORTS
//**********************************************************************************************************************

import java.io.*
import java.util.Arrays

//**********************************************************************************************************************
//  CONSTANT
//**********************************************************************************************************************

val MAS2J_FILE_NAME = "AMW" + ".mas2j"
val JASON_CLASSPATH_BODY = ";(\n|\t| )*classpath:(.|\n)*aslSourcePath:"
val CONFS_PATH = System.getProperty( "user.home" ) + File.separator + ".jason"

//**********************************************************************************************************************
//  MISCELLANEOUS
//**********************************************************************************************************************

plugins {
    java
}

group = "dev.mandrab.uni.magistrale.sistemidistribuiti.amw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral( )
}

dependencies {
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

//**********************************************************************************************************************
//  DEPENDENCIES
//**********************************************************************************************************************

repositories {
    mavenCentral()
    maven( "http://jacamo.sourceforge.net/maven2/" )                                // jason repo
}

// configurations
val junit: Configuration by configurations.creating
val jason: Configuration by configurations.creating
val guava: Configuration by configurations.creating
val apacheCommonsIo: Configuration by configurations.creating
val apacheCommonsLang : Configuration by configurations.creating
val javaMailApi : Configuration by configurations.creating
val activation : Configuration by configurations.creating

val dependencies: List<Configuration> = Arrays.asList( junit, jason )

dependencies {
    junit( "junit", "junit", "4.12" )
    implementation( junit )
    jason( "org.jason-lang", "jason", "2.4" )
    implementation( jason )
    guava( "com.google.guava", "guava", "28.1-jre" )
    implementation( guava )
    apacheCommonsIo( "commons-io", "commons-io", "2.6" )
    implementation( apacheCommonsIo )
    apacheCommonsLang( "org.apache.commons", "commons-lang3", "3.9" )
    implementation( apacheCommonsLang )
    javaMailApi( "com.sun.mail", "javax.mail", "1.6.2" )
    implementation( javaMailApi )
    activation( "javax.activation", "activation", "1.1.1" )
    implementation( activation )
}

//**********************************************************************************************************************
//  TASKS
//**********************************************************************************************************************

task<JavaExec>( "config" ) {

    val antPath = jason.asPath.split( ":" ).firstOrNull { s -> s.contains( ".*/ant-.*".toRegex( ) ) }
    val jadePath = jason.asPath.split( ":" ).firstOrNull { s -> s.contains( ".*/jade-.*".toRegex( ) ) }

    group = "jason"
    sourceSets {                                                    // set cli path in which call the cmd
        main {
            classpath = runtimeClasspath
        }
    }

    standardInput = System.`in`                                     // redirect std input (terminal)

    main = "Configurator"
    args( antPath.orEmpty( ) )
    args( jadePath.orEmpty( ) )
    args( CONFS_PATH )

}

task<JavaExec>( "run_system" ) {

    //addLibraryClasspath( )                                        // add dependecies path to mas2j project file

    group = "jason"                                                 // process (TODO ?) group name
    sourceSets {                                                    // set cli path in which call the cmd
        main {
            classpath = runtimeClasspath
        }
    }

    standardInput = System.`in`                                     // redirect std input (terminal)

    dependsOn( "config" )

    main = "jason.runtime.RunJasonProject"                          // jason mas2j runner
    args( MAS2J_FILE_NAME )                                         // specify project file

}

task<JavaExec>( "run_terminal" ) {

    //addLibraryClasspath(  )                                       // add dependecies classpath to mas2j project file

    group = "jade-terminal"                                         // process (?) group name
    sourceSets {                                                    // set cli path in which call the cmd
        main {
            classpath = runtimeClasspath
        }
    }
    standardInput = System.`in`                                     // std input (terminal)

    main = "controller.Main"                                        // jason mas2j runner

    args( "retry" )                                                 // keep retry connection if fails
}

task<org.gradle.api.internal.AbstractTask> ( "run" ) {
    // runs system in detached mode
    doFirst {
        ProcessBuilder( )
                .directory( projectDir )
                .command( "." + File.separator + "gradlew", "run_system" )
                .start( )
    }

    // after the system startup, run the terminal
    if ( project.hasProperty( "all" ) )
        finalizedBy("run_terminal")
}

tasks.findByName( "test" )!!.dependsOn( "run" )

//**********************************************************************************************************************
//  FUNCTIONS
//**********************************************************************************************************************

// add dependencies classpath to mas2j project file
fun addLibraryClasspath( excluded: List<Regex>? = null ): String {
    // read the actual mas2j project file
    var fileStr = BufferedReader( FileReader( File( MAS2J_FILE_NAME ) ) as Reader ).readText( )

    // generate the new classpath
    val strBuilder = StringBuilder( )

    dependencies.forEach { conf -> conf.asPath                      // get all the path to the libraries
            .split ( ":" )
            .filter { path -> excluded == null                      // if there isn't excluded paths, then do nothing
                    || excluded.stream()                            // else, check if i should exclude the path
                    .filter { e -> path.contains( e )  }
                    .findAny( )
                    .isPresent }
            .forEach { path ->
                strBuilder.append( "\n\t\t\"$path\";" ) }           // add the path to classpath
    }

    // replace the previous classpath
    if ( ! strBuilder.isEmpty() )
        fileStr = fileStr.replace( JASON_CLASSPATH_BODY.toRegex(), ";\n\n\tclasspath:$strBuilder\n\n\taslSourcePath:" )

    // re-write the updated mas2j project file
    val out = BufferedWriter( FileWriter( MAS2J_FILE_NAME ) )
    out.write( fileStr )
    out.flush( )
    out.close( )

    val str = strBuilder.replace("(\"|\t)*".toRegex(), "").replace(";(\n)*".toRegex(), ":")
    if ( str.isNotEmpty(  ) )
        return str.substring(0, str.length- 1)
    return ""
}