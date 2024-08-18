package org.smartregister.fct.serverconfig.presentation.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.smartregister.fct.common.data.manager.AppSettingManager
import org.smartregister.fct.common.domain.model.ServerConfig
import org.smartregister.fct.common.util.componentScope
import org.smartregister.fct.common.util.uuid

class ServerConfigPanelComponent(
    private val componentContext: ComponentContext
) : KoinComponent, ComponentContext by componentContext {

    private val appSettingManager: AppSettingManager by inject()
    private val appSetting = appSettingManager.appSetting

    private var _activeTabIndex = MutableValue(0)
    val activeTabIndex: Value<Int> = _activeTabIndex

    private val _tabComponents = MutableValue<List<ServerConfigComponent>>(listOf())
    val tabComponents: Value<List<ServerConfigComponent>> = _tabComponents

    init {
        loadConfigs()
    }

    fun changeTab(index: Int) {
        _activeTabIndex.value = index
    }

    fun createNewConfig(title: String) {
        val config = ServerConfig(
            id = uuid(),
            title = title
        )
        _tabComponents.value += listOf(
            ServerConfigComponent(
                componentContext = componentContext,
                serverConfig = config
            )
        )
        updateSetting()
    }

    fun closeTab(tabIndex: Int) {
        val updatedConfigComponents = _tabComponents
            .value
            .filterIndexed { index, _ -> index != tabIndex }

        if (activeTabIndex.value > 0 && activeTabIndex.value >= tabIndex) {
            _activeTabIndex.value -= 1
        }

        _tabComponents.value = updatedConfigComponents
        updateSetting()
    }

    private fun loadConfigs() {
        _tabComponents.value = appSetting
            .serverConfigs
            .map {
                ServerConfigComponent(
                    componentContext = componentContext,
                    serverConfig = it,
                )
            }

    }

    private fun updateSetting() {
        val configs = _tabComponents.value.map { it.serverConfig }
        val updatedSetting = appSetting.copy(
            serverConfigs = configs
        )
        appSettingManager.setAndUpdate(updatedSetting)
    }
}