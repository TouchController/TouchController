/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state.test

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ImmutableValueTest::class,
    CombineValueTest::class,
    SubscribeDistinctTest::class,
    StateFlowExtTest::class,
)
class StateTest
