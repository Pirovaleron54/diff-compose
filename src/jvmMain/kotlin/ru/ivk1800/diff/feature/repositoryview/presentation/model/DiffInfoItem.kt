package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DiffInfoItem {
    data class Line(val text: String, val type: Type) : DiffInfoItem {
        enum class Type {
            None,
            Added,
            Removed,
        }
    }

    data class HunkHeader(val text: String) : DiffInfoItem
}