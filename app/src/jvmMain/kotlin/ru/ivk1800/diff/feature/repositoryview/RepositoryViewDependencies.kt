package ru.ivk1800.diff.feature.repositoryview

import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.logging.Logger
import ru.ivk1800.vcs.api.Vcs

data class RepositoryViewDependencies(
    val router: RepositoryViewRouter,
    val vcs: Vcs,
    val logger: Logger,
)
