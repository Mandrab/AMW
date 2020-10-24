package common

object User {

	interface User {

		fun client(): String

		fun email(): String

		fun address(): String
	}
}