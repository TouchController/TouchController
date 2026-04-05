/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.text

import top.fifthlight.touchcontroller.api.v1.text.TextFactory
import top.fifthlight.combine.data.TextFactory as CombineTextFactory

object ApiTextFactory : TextFactory {
    override fun literal(text: String) = ApiText.Literal(text)

    override fun translatable(id: String) = ApiText.Translatable(id)

    override fun format(id: String, vararg args: Any) = ApiText.Raw(CombineTextFactory.format(id, *args))
}
