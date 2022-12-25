package ru.ivk1800.vcs.git

sealed class VcsException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
    class ProcessException(message: String) : VcsException(message, null)
    class ParseException(message: String, cause: Throwable?) : VcsException(message, cause)
}