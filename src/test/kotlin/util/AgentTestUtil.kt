package util

import jade.core.ProfileImpl
import jade.core.Runtime
import jason.control.ExecutionControl
import jason.infra.MASLauncherInfraTier
import jason.mas2j.parser.mas2j
import jason.util.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.PrintStream

/**
 * Utilities methods to test Jade agents
 *
 * @author Paolo Baldini
 */
class AgentTestUtil {
	private lateinit var launcher: MASLauncherInfraTier
	private lateinit var masThread: Thread
	private lateinit var testStream: ByteArrayOutputStream
	private lateinit var oldStream: PrintStream

	fun startMAS(masPath: String) {
		val project = mas2j(FileInputStream(masPath)).mas()

		Config.get().jasonJar ?: let {
			Config.get().setShowFixMsgs(false)
			Config.get().fix()
		}
		project.projectFile = File(masPath)
		project.directory = File(masPath).parentFile.path

		launcher = project.infrastructureFactory.createMASLauncher()
		launcher.setProject(project)
		launcher.writeScripts(false, false)

		masThread = Thread(launcher, "MAS-Launcher")
		masThread.start()

		redirectIO()
		while (masThread.state == Thread.State.RUNNABLE) Thread.sleep(200)
		while (Runtime.instance().createAgentContainer(ProfileImpl()) == null) Thread.sleep(200)
		restoreIO()
	}

	fun endContainer() {
		launcher.stopMAS()
		while (ExecutionControl().isRunning && masThread.state != Thread.State.TERMINATED) {
			Thread.sleep(500)
			launcher.stopMAS()
		}
	}

	/**
	 * Redirect System.err to test-stream
	 */
	fun redirectIO() {
		// Create a stream to hold the output
		testStream = ByteArrayOutputStream()
		val ps = PrintStream(testStream)
		// IMPORTANT: Save the old System.err!
		oldStream = System.err
		// Tell Java to use your special stream
		System.setErr(ps)
	}

	/**
	 * Restore System.err to default error-stream
	 */
	fun restoreIO() {
		// Put things back
		System.err.flush()
		System.setErr(oldStream)
	}
}