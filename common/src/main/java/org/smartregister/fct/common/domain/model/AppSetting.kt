package org.smartregister.fct.common.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSetting(
    val isDarkTheme: Boolean = false,
    val codeEditorConfig: CodeEditorConfig = CodeEditorConfig(),
    val serverConfigs: List<ServerConfig> = listOf()
)
