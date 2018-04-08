package rak.pixellwp.cycling.models

import android.graphics.Bitmap
import android.util.Log
import rak.pixellwp.cycling.jsonModels.PaletteJson
import rak.pixellwp.cycling.jsonModels.TimelineImageJson
import java.util.*

class TimelineImage(json: TimelineImageJson) : PaletteImage {
    private val base: ColorCyclingImage = ColorCyclingImage(json.base)
    private val palettes: List<Palette> = parsePalettes(json.palettes)
    private val timeline: Timeline = Timeline(json.timeline, palettes)
    private var oldTimePassedInSeconds = -1
    private var currentPalette = palettes.first()
    private val logTag = "Timeline Image"

    private fun parsePalettes(jsonPalettes: Map<String, PaletteJson>): List<Palette> {
       return jsonPalettes.map { entry -> Palette(entry.key, entry.value) }.toList()
    }

    override fun advance(timePassed: Int){
        base.palette = getCurrentPalette()
        base.advance(timePassed)
    }

    override fun getBitmap() : Bitmap {
        return base.getBitmap()
    }

    override fun getImageWidth(): Int {
        return base.getImageWidth()
    }

    override fun getImageHeight(): Int {
        return base.getImageHeight()
    }

    private fun getCurrentPalette() : Palette {
        val currentTime = getTimePassedInSeconds()
        if (oldTimePassedInSeconds == currentTime){
            return currentPalette
        }
        oldTimePassedInSeconds = currentTime

        val previous = timeline.getPreviousPalette(currentTime)
        if (currentTime == previous.key){
            currentPalette = previous.value
            return currentPalette
        }
        val next = timeline.getNextPalette(currentTime)
        if (currentTime == next.key){
            currentPalette = next.value
            return currentPalette
        }

        val totalDist = next.key - previous.key
        if (totalDist == 0){
            currentPalette = next.value
            return currentPalette
        }

        val current = currentTime - previous.key
        val percent: Int = (current/totalDist) * precisionInt

        Log.d(logTag, "Blending palettes for ${previous.key} and ${next.key} with current time $currentTime and percent blend $percent")
        currentPalette = previous.value.blendPalette(next.value, percent)
        return currentPalette
    }

    private fun getTimePassedInSeconds() : Int {
        return Calendar.getInstance().get(Calendar.SECOND)
    }
}