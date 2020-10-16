package common.type

/**
 * Enum class mainly used for debug purpose.
 * In real system it would obviously be changed
 *
 * @author Paolo Baldini
 */
enum class User {
	CLIENT,                             // run application in client mode (orders)
	ADMIN,                              // run application in admin mode (item and command tabs)
	DEBUG                               // run application in debug mode (both client and admin interfaces)
}