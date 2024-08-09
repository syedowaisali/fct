package org.smartregister.fct.sm.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import org.smartregister.fct.editor.data.enums.CodeStyle
import org.smartregister.fct.editor.ui.CodeEditor
import org.smartregister.fct.radiance.ui.components.Tab
import org.smartregister.fct.radiance.ui.components.TabRow
import org.smartregister.fct.radiance.ui.components.dialog.rememberConfirmationDialog
import org.smartregister.fct.sm.data.provider.SMTabViewModelProvider
import org.smartregister.fct.sm.data.viewmodel.SMViewModel
import org.smartregister.fct.sm.domain.model.SMDetail
import org.smartregister.fct.sm.ui.components.SMUploadButton

class StructureMapScreen : Screen {

    @Composable
    override fun Content() {

        val scope = rememberCoroutineScope()
        val viewModel = getKoin().get<SMViewModel>()
        val smTabViewModelProvider = getKoin().get<SMTabViewModelProvider>()
        var tabIndex by remember { mutableStateOf(0) }
        val smDetailList = viewModel.getAllSMList().collectAsState(initial = listOf())

        smDetailList.value.forEachIndexed { index, smDetail ->
            smTabViewModelProvider.addSMTabViewModel(smDetail)

            LaunchedEffect(null) {
                if (index == 0) {
                    smTabViewModelProvider.updateActiveSMTabViewModel(smDetail.id)
                }
            }
        }

        val smDeleteConfirmDialogController = rememberConfirmationDialog<SMDetail>(
            title = "Delete Structure Map",
            message = "Are you sure you want to delete this structure-map?"
        ) { smDetail ->
            smDetail?.run {
                smTabViewModelProvider.removeSMTabViewModel(smDetail.id)
                viewModel.delete(smDetail.id)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = tabIndex,
            ) {
                smDetailList.value.forEachIndexed { index, smDetail ->

                    Tab(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    smDetail.title,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clickable(
                                            onClick = {
                                                smDeleteConfirmDialogController.show(
                                                    smDetail
                                                )
                                            },
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = rememberRipple(
                                                bounded = false,
                                                radius = 15.dp
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Rounded.Close,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        selected = index == tabIndex,
                        onClick = {
                            scope.launch {
                                smTabViewModelProvider.updateActiveSMTabViewModel(smDetail.id)
                                tabIndex = index
                            }
                        }
                    )
                }
            }

            val activeSMTabViewModel by smTabViewModelProvider.getActiveSMTabViewModel().collectAsState()

            activeSMTabViewModel
                ?.let { smTabViewModel ->
                    CodeEditor(
                        codeStyle = CodeStyle.StructureMap,
                        controller = smTabViewModel.codeController
                    )
            } ?: SMUploadButton()
        }
    }

}

@Composable
private fun UploadSMButton() {
    var showButton by remember { mutableStateOf(false) }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = showButton,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
    ) {
        Box(Modifier.fillMaxSize()) {
            SMUploadButton(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    LaunchedEffect(showButton) {
        delay(300)
        showButton = true
    }
}