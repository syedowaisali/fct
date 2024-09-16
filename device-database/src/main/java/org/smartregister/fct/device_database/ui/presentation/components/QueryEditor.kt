package org.smartregister.fct.device_database.ui.presentation.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.smartregister.fct.adb.domain.usecase.DeviceManager
import org.smartregister.fct.aurora.presentation.ui.components.Button
import org.smartregister.fct.aurora.presentation.ui.components.LinearIndicator
import org.smartregister.fct.device_database.data.persistence.DeviceDBConfigPersistence
import org.smartregister.fct.device_database.data.transformation.SQLQueryTransformation
import org.smartregister.fct.device_database.ui.components.QueryTabComponent
import org.smartregister.fct.engine.data.manager.AppSettingManager
import org.smartregister.fct.engine.domain.model.AppSetting
import org.smartregister.fct.engine.util.prettyJson
import org.smartregister.fct.logger.FCTLogger

@Composable
internal fun QueryEditor(component: QueryTabComponent) {

    val appSetting: AppSetting = koinInject<AppSettingManager>().appSetting
    val isDarkTheme = appSetting.isDarkTheme
    var lineNumbers by remember(component) { mutableStateOf("") }
    var lineNumbersTopPadding by remember(component) { mutableStateOf(18.dp) }
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    LaunchedEffect(component) {
        lineNumbers = getLineNumbers(component.query.value.text)
    }

    lineNumbersTopPadding = if (lineNumbers.contains("2")) {
        16.dp
    } else {
        19.dp
    }

    Box {
        if (component.loading.collectAsState().value) {
            LinearIndicator()
        }
        Row {
            Box(
                modifier = Modifier.widthIn(min = 30.dp).fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                    .padding(start = 5.dp, top = lineNumbersTopPadding, end = 5.dp, bottom = 4.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .verticalScroll(verticalScrollState),
                    text = lineNumbers,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                )
            }

            Box {
                Row(
                    Modifier.horizontalScroll(horizontalScrollState).widthIn(max = 2000.dp)
                        .fillMaxHeight()
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                            .pointerHoverIcon(PointerIcon.Text).onPreviewKeyEvent { keyEvent ->
                                when {
                                    keyEvent.isCtrlPressed && keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp -> {
                                        component.runQuery()
                                        true
                                    }

                                    else -> false
                                }
                            },
                        value = component.query.collectAsState().value,
                        onValueChange = {
                            lineNumbers = getLineNumbers(it.text)
                            component.updateTextField(it)
                        },

                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Start,
                        ),
                        singleLine = false,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                alpha = 0.3f
                            ),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                alpha = 0.3f
                            ),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        visualTransformation = SQLQueryTransformation(
                            isDarkTheme = isDarkTheme,
                            colorScheme = MaterialTheme.colorScheme,
                        )
                    )
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = verticalScrollState
                    )
                )

                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = horizontalScrollState
                    )
                )
            }
        }
    }
}

private fun getLineNumbers(text: String): String {
    return List(
        "\n".toRegex().findAll(text).toList().size + 1
    ) { index ->
        "${index + 1}"
    }.joinToString("\n")
}