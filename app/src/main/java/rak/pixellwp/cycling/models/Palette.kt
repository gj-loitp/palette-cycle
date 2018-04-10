package rak.pixellwp.cycling.models

import android.graphics.Color
import rak.pixellwp.cycling.jsonModels.PaletteJson

class Palette(val id: String, colors: List<Int>, val cycles: List<Cycle>) {
    constructor(colors: List<Int>, cycles: List<Cycle>) : this("", colors, cycles)
    constructor(id: String, paletteJson: PaletteJson) : this(id, paletteJson.parsedColors, paletteJson.cycles)

    private val baseColors = colors
    var colors = baseColors.toMutableList()

    fun blendPalette(next: Palette, percent: Int): Palette {
        val mixedPalette = Palette(this.baseColors, this.cycles)

        for (i in 0 until baseColors.size){
            mixedPalette.colors[i] = fadeColors(baseColors[i], next.baseColors[i], percent)
        }

        return mixedPalette
    }

    fun cycle(timePassed: Int) {
        //it's important we copy the base values as each time we cycle it 'starts from 0'; it's not additive
        colors = baseColors.toMutableList()
        cycles
                .filter { it.rate != 0 }
                .forEach { cycle ->
                    cycle.reverseColorsIfNecessary(colors)
                    val amount = cycle.getCycleAmount(timePassed)
                    blendShiftColors(colors, cycle, amount)
                    cycle.reverseColorsIfNecessary(colors)
                }
    }

    private fun shiftColors(colors: MutableList<Int>, cycle: Cycle, amount: Double) {
        val intAmount = amount.toInt()
        for (i in 0 until intAmount) {
            val temp = colors[cycle.high]
            for (j in (cycle.high - 1) downTo cycle.low) {
                colors[j + 1] = colors[j]
            }
            colors[cycle.low] = temp
        }
    }

    // BlendShift Technology conceived, designed and coded by Joseph Huckaby
    private fun blendShiftColors(colors: MutableList<Int>, cycle: Cycle, amount: Double) {
        shiftColors(colors, cycle, amount)

        val remainder = Math.floor((amount - Math.floor(amount)) * precision).toInt()
        val temp = colors[cycle.high]
        for (j in (cycle.high - 1) downTo cycle.low) {
            colors[j + 1] = fadeColors(colors[j+1], colors[j], remainder)
        }
        colors[cycle.low] = temp
    }

    private fun fadeColors(sourceColor: Int, destColor: Int, frame: Int): Int {
        val amount = Math.min(precisionInt, Math.max(0, frame))

        val red = blendColor(Color.red(sourceColor), Color.red(destColor), amount)
        val green = blendColor(Color.green(sourceColor), Color.green(destColor), amount)
        val blue = blendColor(Color.blue(sourceColor), Color.blue(destColor), amount)

        return Color.rgb(red, green, blue)
    }

    private fun blendColor(source: Int, dest: Int, amount: Int) : Int {
        return source + (((dest - source) * amount) / precisionInt)
    }


}
