package common

/**
 * Possible requests that can run through application
 *
 * @author Paolo Baldini
 */
enum class Request {
	ADD_COMMAND,        // push a command to repo
	ADD_VERSION,        // push a version to repo
	END,                // shutdown system
	EXEC_COMMAND,       // exec a command
	EXEC_SCRIPT,        // exec a script
	PLACE_ORDER         // make an order
}