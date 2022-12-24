package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsFile
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class GitVcs : Vcs {
    private val parser = VcsParser()
    private val diffParser = VcsDiffParser()

    override suspend fun isRepository(directory: File): Boolean =
        withContext(Dispatchers.IO) {
            if (!directory.exists()) {
                false
            } else {
                val isRepo = createProcess(directory, "git rev-parse --git-dir").exitValue() == 0
                isRepo && Path(directory.path, ".git").exists()
            }
        }

    override suspend fun getCommits(
        directory: File,
        branchName: String,
        limit: Int,
        offset: Int,
    ): List<VcsCommit> = withContext(Dispatchers.IO) {
        val process = createProcess(
            directory,
            "git reflog --pretty={$FIELDS}, -n $limit, --skip $offset $branchName",
        )

        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()
        println(error)
        parser.parseCommits("[$result]")
    }

    override suspend fun getCommit(directory: File, hash: String): VcsCommit? {
        val process = createProcess(
            directory,
            "git log -1 --format={$FIELDS} $hash",
        )
        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()
        println(error)
        println(result)

        return parser.parseCommit(result)
    }

    override suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile> {
        val process = createProcess(
            directory,
            "git show --name-only $commitHash -n 1 --oneline",
        )

        val result = process.inputStream.reader().readText()
        return parser.parseFiles(result)
    }

    override suspend fun getDiff(
        directory: File,
        oldCommitHash: String,
        newCommitHash: String,
        filePath: String,
    ): VcsDiff {
        val command = "git diff $oldCommitHash..$newCommitHash $filePath"
        val process = createProcess(
            directory,
            command,
        )

        return runProcess(process,
            onError = { error ->
                VcsException.ProcessException(error)
            }, onResult = { result ->
                diffParser.parse(result)
            }
        )
    }

    private inline fun <T> runProcess(
        process: Process,
        onResult: (result: String) -> T,
        onError: (error: String) -> Throwable,
    ): T = if (process.exitValue() != 0) {
        val error = process.errorStream.reader().readText()
        throw onError.invoke(error)
    } else {
        val result = process.inputStream.reader().readText()
        if (result.isEmpty()) {
            throw onError.invoke("result of process is empty")
        } else {
            onResult.invoke(result)
        }
    }

    private fun createProcess(directory: File, command: String): Process =
        ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(directory)
            .start()
            .apply { waitFor(10, TimeUnit.SECONDS) }

    private companion object {
        val FIELDS = listOf(
            """"hash":"%H"""",
            """"parents":"%P"""",
            """"abbreviatedHash":"%h"""",
            """"authorName":"%an"""",
            """"authorEmail":"%ae"""",
            """"authorDate":"%at"""",
            """"commiterName":"%cn"""",
            """"commiterEmail":"%ce"""",
            """"commiterDate":"%ct"""",
            """"message":"%B"""",
        ).joinToString(",")
    }
}
