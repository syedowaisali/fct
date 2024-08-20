package org.smartregister.fct.serverconfig.domain.model

import org.smartregister.fct.common.domain.model.ServerConfig
import org.smartregister.fct.serverconfig.presentation.components.ExportConfigDialogComponent

sealed class ExportDialogState {

    data object Idle : ExportDialogState()

    data class SelectConfigs(
        val component: ExportConfigDialogComponent,
        val configs: List<ServerConfig>,
    ) : ExportDialogState()

    data class ExportFileDialog(
        val configJson: String,
    ) : ExportDialogState()

    data object ExportCompleteDialog : ExportDialogState()
}