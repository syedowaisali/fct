package org.smartregister.fct.sm.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.smartregister.fct.aurora.domain.controller.DialogController
import org.smartregister.fct.aurora.ui.components.TextButton
import org.smartregister.fct.aurora.ui.components.dialog.DialogType
import org.smartregister.fct.aurora.ui.components.dialog.getOrDefault
import org.smartregister.fct.aurora.ui.components.dialog.rememberDialog
import org.smartregister.fct.editor.data.enums.FileType
import org.smartregister.fct.fm.ui.dialog.rememberFileProviderDialog
import org.smartregister.fct.sm.presentation.component.TabComponent

@Composable
internal fun AddSourceButton(component: TabComponent) {
    val scope = rememberCoroutineScope()
    val smDetail = component.smDetail
    val sourceName by component.sourceName.subscribeAsState()

    val parsingErrorDialog = parseErrorDialog()
    val filePickerDialog = fileProviderDialog(
        scope = scope,
        component = component,
        parsingErrorDialog = parsingErrorDialog
    )

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        label = sourceName,
        onClick = {
            scope.launch {
                val title = sourceName
                val initialData = smDetail.source ?: ""
                filePickerDialog.show(title, initialData)
            }
        }
    )
}

@Composable
private fun parseErrorDialog() = rememberDialog(
    title = "Parsing Error",
    width = 300.dp,
    dialogType = DialogType.Error
) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        text = it.getExtra().getOrDefault(0, ""),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun fileProviderDialog(
    scope: CoroutineScope,
    component: TabComponent,
    parsingErrorDialog: DialogController
) = rememberFileProviderDialog(
    fileType = FileType.Json
) { source ->

    scope.launch {
        val result = component.addSource(source)
        if (result.isFailure) {
            parsingErrorDialog.show(result.exceptionOrNull()?.message)
        }
    }
}