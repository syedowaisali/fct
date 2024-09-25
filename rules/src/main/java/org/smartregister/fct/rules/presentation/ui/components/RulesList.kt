package org.smartregister.fct.rules.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.smartregister.fct.aurora.presentation.ui.components.Icon
import org.smartregister.fct.rules.presentation.components.RulesScreenComponent

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RulesList(
    component: RulesScreenComponent,
) {
    var expanded by remember { mutableStateOf(false) }
    val hasActiveWorkspace by component.workspace.collectAsState()

    Box {
        Chip(
            border = BorderStroke(
                width = 0.5.dp,
                color = if (hasActiveWorkspace != null) colorScheme.onSurface.copy(0.6f) else colorScheme.onSurface.copy(0.4f)
            ),
            modifier = Modifier.width(300.dp),
            onClick = {
                expanded = true && hasActiveWorkspace != null
            },
            colors = ChipDefaults.chipColors(
                backgroundColor = colorScheme.surface,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rules",
                    color = colorScheme.onSurface
                )
                Icon(
                    icon = Icons.Outlined.ArrowDropDown
                )
            }
        }

        DropdownMenu(
            modifier = Modifier
                .width(300.dp)
                .requiredHeightIn(max = 700.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            component.ruleWidgets.value.forEach { widget ->
                DropdownMenuItem(
                    text = {
                        Text(widget.body.name)
                    },
                    onClick = {
                        expanded = false
                        component.focus(widget)
                    }
                )
            }
        }
    }

}