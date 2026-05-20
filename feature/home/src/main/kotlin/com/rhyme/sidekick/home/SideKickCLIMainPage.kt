package com.rhyme.sidekick.home

import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicMain
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text


class SideKickCLIMainPage {

    fun main() = runMosaicMain {
        Column {
            Text("Hello Rhyme!")
            Spacer(modifier = Modifier.height(1))
        }
    }
}
