package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsTableInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.FilesInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState

class HistoryStateComposer(
    private val filesInfoInteractor: FilesInfoInteractor,
    private val commitsTableInteractor: CommitsTableInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
) {
    fun getState(scope: CoroutineScope): StateFlow<HistoryState> =
        combine(
            filesInfoInteractor.state,
            commitsTableInteractor.state,
            diffInfoInteractor.state,
        ) { activeState, commitsTableState, diffInfoState ->
            HistoryState(
                commitsTableState = commitsTableState,
                diffInfoState = diffInfoState,
                filesInfoState = activeState,
            )
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = getDefaultState(),
            )

    fun getDefaultState(): HistoryState =
        HistoryState(
            commitsTableState = CommitsTableState.Loading,
            diffInfoState = DiffInfoState.None,
            filesInfoState = FilesInfoState.None,
        )
}