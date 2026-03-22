package top.fifthlight.touchcontroller.common.ui.layer.tab.all

import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.touchcontroller.common.ui.layer.tab.builtin.BuiltInTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.CustomTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.holdingitem.HoldingItemTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.ridingentity.RidingEntityTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.selectedentity.SelectingEntityTab

val allLayerConditionTabs = persistentListOf(
    BuiltInTab,
    HoldingItemTab,
    RidingEntityTab,
    SelectingEntityTab,
    CustomTab,
)
