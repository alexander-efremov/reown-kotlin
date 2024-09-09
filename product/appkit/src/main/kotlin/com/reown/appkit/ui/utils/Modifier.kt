package com.reown.appkit.ui.utils

import androidx.compose.ui.Modifier

internal fun Modifier.conditionalModifier(isConditional: Boolean, modifier: Modifier.() -> Modifier): Modifier = if (isConditional) then(modifier()) else this
