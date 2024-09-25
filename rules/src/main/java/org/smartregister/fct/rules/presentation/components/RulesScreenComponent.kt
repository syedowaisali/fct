package org.smartregister.fct.rules.presentation.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.smartregister.fct.adb.domain.usecase.DeviceManager
import org.smartregister.fct.common.presentation.component.ScreenComponent
import org.smartregister.fct.engine.util.componentScope
import org.smartregister.fct.engine.util.decodeJson
import org.smartregister.fct.engine.util.encodeJson
import org.smartregister.fct.engine.util.uuid
import org.smartregister.fct.rules.data.enums.Placement
import org.smartregister.fct.rules.domain.model.Workspace
import org.smartregister.fct.rules.domain.model.DataSource
import org.smartregister.fct.rules.domain.model.RequestBundle
import org.smartregister.fct.rules.domain.model.Widget
import org.smartregister.fct.rules.domain.model.Rule
import org.smartregister.fct.rules.domain.model.RuleResponse
import org.smartregister.fct.rules.domain.usecase.CreateNewWorkspace
import org.smartregister.fct.rules.domain.usecase.UpdateWorkspace
import org.smartregister.fct.rules.util.WorkspaceConfig
import java.io.Serializable

class RulesScreenComponent(componentContext: ComponentContext) :
    ScreenComponent, KoinComponent, ComponentContext by componentContext {

    internal val boardSize = Size(15000f, 15000f)

    private val createNewWorkspace: CreateNewWorkspace by inject()
    private val updateWorkspace: UpdateWorkspace by inject()

    private var _error = MutableSharedFlow<String?>()
    internal val error: SharedFlow<String?> = _error

    private var _info = MutableSharedFlow<String?>()
    internal val info: SharedFlow<String?> = _info

    private var _loading = MutableStateFlow(false)
    internal val loading: StateFlow<Boolean> = _loading

    private var _boardScaling = MutableStateFlow(1f)
    internal val boardScaling: StateFlow<Float> = _boardScaling

    private var _boardOffset = MutableSharedFlow<IntOffset>()
    internal val boardOffset: SharedFlow<IntOffset> = _boardOffset

    private var _activeWorkspace = MutableStateFlow(WorkspaceConfig.workspace)
    internal val workspace: StateFlow<Workspace?> = _activeWorkspace

    private val _dataSourceWidgets = MutableStateFlow(listOf<Widget<DataSource>>())
    internal val dataSourceWidgets: StateFlow<List<Widget<DataSource>>> = _dataSourceWidgets

    private val _ruleWidgets = MutableStateFlow(listOf<Widget<Rule>>())
    internal val ruleWidgets: StateFlow<List<Widget<Rule>>> = _ruleWidgets

    init {
        componentScope.launch {
            _activeWorkspace.collectLatest {
                it?.let {
                    _dataSourceWidgets.emit(it.dataSources)
                    _ruleWidgets.emit(it.rules)
                    it.rules.forEach(::findParents)
                }
            }
        }
    }

    internal fun updateBoardOffset(offset: IntOffset) {
        componentScope.launch {
            _boardOffset.emit(offset)
        }
    }

    internal fun addDataSource(dataSource: DataSource) {
        componentScope.launch {

            if (dataSource.id.trim().isNotEmpty() && dataSource.id in _dataSourceWidgets.value.map { it.body.id }) {
                _error.emit("${dataSource.id} id is already exists")
                return@launch
            }

            if (dataSource.resourceType.trim().isNotEmpty() && dataSource.resourceType in _dataSourceWidgets.value.map { it.body.resourceType }) {
                _error.emit("${dataSource.resourceType} resourceType is already exists")
                return@launch
            }

            _dataSourceWidgets.emit(
                _dataSourceWidgets.value.toMutableList().apply {
                    add(Widget(dataSource))
                }
            )
        }
    }

    internal fun addRule(rule: Rule) {
        componentScope.launch {

            if (rule.name in _ruleWidgets.value.map { it.body.name }) {
                _error.emit("${rule.name} rule name is already exists")
                return@launch
            }

            val ruleWidget = Widget(
                body = rule,
            )

            val findParentWidgets = findParents(ruleWidget)
            val parentWidget = findParentWidgets
                .filter { it.body is Rule }
                .sortedWith(compareByDescending { (it.body as Rule).priority })
                .firstOrNull() ?: findParentWidgets.lastOrNull { it.body is DataSource }

            if (parentWidget != null) {
                if (parentWidget.placement == Placement.Left) {
                    ruleWidget.x = parentWidget.x - (parentWidget.size.width + 100)
                } else {
                    ruleWidget.x = parentWidget.x + parentWidget.size.width + 100
                }
                ruleWidget.y = parentWidget.y + parentWidget.size.height
            } else {
                ruleWidget.x = boardSize.width / 2f + 100f
                ruleWidget.y = boardSize.height / 2f
            }

            _ruleWidgets.emit(
                _ruleWidgets.value.toMutableList().apply { add(ruleWidget) }
            )

            delay(200)
            focus(ruleWidget)
        }
    }

    internal fun removeDataSource(widget: Widget<DataSource>) {
        componentScope.launch {
            _dataSourceWidgets.emit(
                _dataSourceWidgets.value.toMutableList().apply {
                    remove(widget)
                }
            )
            focusCenter()
        }
    }

    internal fun removeRule(ruleWidget: Widget<Rule>) {
        componentScope.launch {
            val parentWidget = findParents(ruleWidget)
            _ruleWidgets.emit(
                _ruleWidgets.value.filter {
                    it.body.id != ruleWidget.body.id
                }
            )
            parentWidget.lastOrNull()?.let(::focus)
        }
    }

    internal fun findParents(widget: Widget<Rule>) : List<Widget<out Serializable>> {
        //if (widget.parents.isNotEmpty()) return widget.parents

        val dsWidgets = _dataSourceWidgets.value.filter { dsWidget ->

            widget.body.actions.any {
                "${dsWidget.body.id.trim().ifEmpty { dsWidget.body.resourceType }}(?=\\s*([,).]))".toRegex().find(it) != null
            }
        }

        val ruleWidgets = _ruleWidgets.value.filter { ruleWidget ->
            widget.body.actions.any {
                "data\\.get\\([\"']${ruleWidget.body.name}[\"']\\)".toRegex().find(it) != null
            }
        }

        widget.parents = dsWidgets + ruleWidgets

        val warnings = mutableListOf<String>()
        if (widget.parents.isEmpty()) {
            warnings.add("No parent available")
        }

        ruleWidgets.minOfOrNull { it.body.priority }?.takeIf { it >= widget.body.priority }?.run {
            warnings.add("Set priority to ${this + 1}")
        }

        widget.warnings = warnings

        return widget.parents
    }

    internal fun executeRules() {
        if (_loading.value) return

        componentScope.launch {

            val device = DeviceManager.getActiveDevice()

            if (device == null) {
                _error.emit("No Device Selected")
                return@launch
            }

            _loading.emit(true)
            val requestBundle = RequestBundle(
                dataSources = _dataSourceWidgets.value.map { it.body },
                rules = _ruleWidgets.value.map { it.body }.map { rule ->
                    rule.copy(
                        actions = rule.actions.map {
                            "data.put('${rule.name}', $it)"
                        }
                    )
                }
            )

            val result = device.executeRules(requestBundle.encodeJson())
            _loading.emit(false)

            if (result.isSuccess) {
                val ruleResponse = result.getOrThrow().toString().decodeJson<RuleResponse>()
                if (ruleResponse.error != null) {
                    _error.emit(ruleResponse.error)
                } else {
                    _ruleWidgets.value.forEach {
                        it.body.result = ruleResponse.result[it.body.name]!!
                    }
                }
            } else {
                _error.emit(result.exceptionOrNull()?.message ?: "Execution error")
            }
        }
    }

    internal fun createWorkspace(workspace: Workspace) {
        componentScope.launch {
            saveWorkspace()
            createNewWorkspace(workspace)
            openWorkspace(workspace)
        }
    }

    internal fun openWorkspace(workspace: Workspace) {
        WorkspaceConfig.workspace = workspace
        componentScope.launch {
            _activeWorkspace.emit(workspace)
        }
    }

    suspend fun saveWorkspace() {
        val activeWorkspace = _activeWorkspace.firstOrNull()
        if (activeWorkspace != null) {
            updateWorkspace(
                workspace = activeWorkspace.copy(
                    dataSources = _dataSourceWidgets.value,
                    rules = _ruleWidgets.value
                )
            )
        }
    }

    internal fun focus(widget: Widget<out Serializable>) {
        updateBoardOffset(
            IntOffset(
                x = (boardSize.width / 2 - widget.x - widget.size.width / 2f).toInt(),
                y = (boardSize.height / 2 - widget.y - widget.size.height / 2f * Math.random()).toInt()
            )
        )
    }

    internal fun changeBoardScale(scale: Float) {
        componentScope.launch {
            _boardScaling.emit(1f / 100f * scale)
        }
    }

    internal fun focusCenter() {
        updateBoardOffset(IntOffset.Zero)
    }

    internal fun showInfo(text: String) {
        componentScope.launch {
            _info.emit(text)
        }
    }

    internal fun importRules(text: String) {
        componentScope.launch {
            try {

                val rulesJsonArray = JSONArray(text).filterIsInstance<JSONObject>()
                val rulesName = _ruleWidgets.value.map { it.body.name }.toMutableSet()
                val rulesList = mutableListOf<Widget<Rule>>()

                rulesJsonArray.forEachIndexed { index, obj ->

                    val name = obj.getString("name")

                    // skip duplicate rule name
                    if (name !in rulesName) {
                        rulesName.add(name)
                        rulesList.add(
                            Widget(
                                body = Rule(
                                    id = uuid(),
                                    name = obj.getString("name"),
                                    condition = obj.getString("condition"),
                                    priority = obj.optInt("priority", 1),
                                    description = obj.optString("description", ""),
                                    actions = obj.getJSONArray("actions").filterIsInstance<String>().map {
                                        it.replace("data\\.put\\([\"']\\w+[\"']\\s*,\\s*".toRegex(), "").dropLast(1)
                                    }
                                ),
                                placement = if (index < rulesJsonArray.size / 2) Placement.Left else Placement.Right
                            )
                        )
                    }
                }

                // place rule widgets on left side
                rulesList.filter { it.placement == Placement.Left }.let {
                    var x = boardSize.width / 2 - 500
                    var y = boardSize.height / 2 - 330
                    var i = 0

                    it.forEachIndexed { index, widget ->
                        widget.x = x + index
                        widget.y = y + (i++ * 170)

                        if (index > 0 && index % 5 == 0) {
                            x -= 400
                            y = boardSize.height / 2 - 330
                            i = 0
                        }
                    }
                }

                // place rule widgets on right side
                rulesList.filter { it.placement == Placement.Right }.let {
                    var x = boardSize.width / 2 + 200
                    var y = boardSize.height / 2 - 330
                    var i = 0

                    it.forEachIndexed { index, widget ->
                        widget.x = x - index
                        widget.y = y + (i++ * 170)

                        if (index > 0 && index % 5 == 0) {
                            x += 400
                            y = boardSize.height / 2 - 330
                            i = 0
                        }
                    }
                }

                _ruleWidgets.emit(
                    _ruleWidgets.value.toMutableList().apply {
                        addAll(rulesList)
                    }
                )

                focusCenter()
            } catch (ex: Exception) {
                _error.emit(ex.message ?: "Import Error")
            }
        }
    }

    internal fun getRulesJsonString() : String {
        return _ruleWidgets.value
        .map { it.body }
        .map { rule ->
            rule.copy(
                actions = rule.actions.map {
                    "data.put('${rule.name}', $it)"
                }
            )
        }.map {
            linkedMapOf<String, Any>().apply {
                put("name", it.name)
                put("condition", it.condition)
                if (it.description.trim().isNotEmpty()) {
                    put("description", it.description)
                }
                put("priority", it.priority)
                put("actions", it.actions)
            }
        }
        .let { JSONObject().apply { put("rules", it) } }.toString()
    }
}