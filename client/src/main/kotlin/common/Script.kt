package common

object Script {

    interface Script {

        fun script(): String

        fun requirements(): Set<String>
    }
}