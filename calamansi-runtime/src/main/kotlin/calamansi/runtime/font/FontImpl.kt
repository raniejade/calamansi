package calamansi.runtime.font

import calamansi.font.Font
import org.jetbrains.skija.Typeface

data class FontImpl(val typeface: Typeface) : Font