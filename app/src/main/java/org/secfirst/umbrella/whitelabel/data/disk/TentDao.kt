package org.secfirst.umbrella.whitelabel.data.disk

import android.util.Log
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.TextProgressMonitor
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.secfirst.umbrella.whitelabel.misc.AppExecutors.Companion.ioContext
import java.io.File
import java.io.PrintWriter
import java.util.*


interface TentDao {

    suspend fun cloneRepository(url: String): Boolean {
        println("url - $url")
        var result = true
        try {
            withContext(ioContext) {
                if (isNotRepository()) {
                    Git.cloneRepository()
                            .setURI(url)
                            .setDirectory(File(getPathRepository()))
                            .setBranchesToClone(Arrays.asList(BRANCH_NAME))
                            .setProgressMonitor(TextProgressMonitor(PrintWriter(System.out)))
                            .setBranch(BRANCH_NAME)
                            .call()
                }
            }
        } catch (e: Exception) {
            result = false
            File(getPathRepository()).deleteRecursively()
            Log.i(TentDao::class.java.name,
                    "Repository wasn't created - ${isNotRepository()} " +
                            "id - ${getPathRepository()}")
        }
        return result
    }

    suspend fun rebaseBranch(): List<Pair<String, File>> {
        val files = mutableListOf<Pair<String, File>>()
        val git = Git.open(File("${getPathRepository()}/.git"))
        withContext(ioContext) {
            git.checkout().setName("master").call()
            val branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
            branches.forEach { branch ->
                if (BRANCH_NAME == branch.name) {
                    git.pull().setRemoteBranchName("master").setRebase(true).call()
                    files.addAll(getUpdateFiles(git))
                }

            }
        }
        return files
    }

    private fun getUpdateFiles(git: Git): MutableList<Pair<String, File>> {
        val files = mutableListOf<Pair<String, File>>()
        val reader = git.repository.newObjectReader()

        val oldTreeIt = CanonicalTreeParser()
        val oldTree = git.repository.resolve("HEAD~1^{tree}")
        oldTreeIt.reset(reader, oldTree)
        val newTreeIt = CanonicalTreeParser()
        val newTree = git.repository.resolve("HEAD^{tree}")
        newTreeIt.reset(reader, newTree)

        val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
        diffFormatter.setRepository(git.repository)
        val entries = diffFormatter.scan(oldTreeIt, newTreeIt)


        for (entry in entries) {
            val absoluteFilePath = getPathRepository() + entry.newPath
            val pairFile = Pair<String, File>(entry.newPath, File(absoluteFilePath))
            println("file ID - ${entry.newId.toObjectId().name}")

            files.add(pairFile)
        }
        return files
    }

    fun filterElements(): List<Pair<String, File>> {
        val files = mutableListOf<Pair<String, File>>()
        val predicateLanguage = defaultTentLanguage()
        File("${getPathRepository()}$predicateLanguage")
                .walk()
                .filter { !it.path.contains(".git") }
                .filter {
                    it.nameWithoutExtension == TypeFile.SEGMENT.value ||
                            it.nameWithoutExtension == TypeFile.CHECKLIST.value ||
                            it.extension == TypeFile.IMG_CATEGORY.value
                }
                .toList()
                .reversed()
                .forEach { files.add(Pair(it.path, it)) }
        return files.toList()
    }

    fun filterForms(): List<Pair<String, File>> {
        val files = mutableListOf<Pair<String, File>>()
        val predicateLanguage = defaultTentLanguage()
        File("${getPathRepository()}$predicateLanguage")
                .walk()
                .filter { !it.path.contains(".git") }
                .filter { it.nameWithoutExtension == TypeFile.FORM.value }
                .toList()
                .forEach { files.add(Pair(it.path, it)) }
        return files.toList()
    }

    suspend fun filterCategories(): List<Pair<String, File>> {
        val files = mutableListOf<Pair<String, File>>()
        val predicateLanguage = defaultTentLanguage()
        File("${getPathRepository()}$predicateLanguage")
                .walkTopDown()
                .filter { !it.path.contains(".git") }
                .filter { it.nameWithoutExtension == TypeFile.CATEGORY.value }
                .toList()
                .reversed()
                .forEach { files.add(Pair(it.path, it)) }
        return files.toList()
    }

    private fun getDiffEntry(): List<DiffEntry> {
        val git = Git.open(File("${getPathRepository()}/.git"))
        val reader = git.repository.newObjectReader()
        val newTreeIterator = CanonicalTreeParser()
        val newTree = git.repository.resolve("HEAD^{tree}")
        newTreeIterator.reset(reader, newTree)
        val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
        diffFormatter.setRepository(git.repository)
        return diffFormatter.scan(null, newTree)
    }
}

fun String.filterImageCategoryFile(): String {
    val imgFile = File(getPathRepository())
            .walk()
            .filter { file -> !file.path.contains(".git") }
            .filter { file -> file.name == this }
            .filter { it.isFile }
            .toList()
    return if (imgFile.isNotEmpty()) imgFile.last().path else ""
}