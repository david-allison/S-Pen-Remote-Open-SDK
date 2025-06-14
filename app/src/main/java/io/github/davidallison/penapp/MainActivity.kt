/**
 * Copyright 2025 Brayan Oliveira <69634269+brayandso@users.noreply.github.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.davidallison.penapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.davidallison.android.sdk.penremote.AirMotionEvent
import io.github.davidallison.android.sdk.penremote.ButtonEvent
import io.github.davidallison.android.sdk.penremote.SPenEventListener
import io.github.davidallison.android.sdk.penremote.SPenRemote
import io.github.davidallison.android.sdk.penremote.SPenUnitManager
import io.github.davidallison.android.sdk.penremote.SPenUnitType


class MainActivity : AppCompatActivity() {
    private var connectedTextView: TextView? = null
    private var buttonFeedback: TextView? = null
    private var deltaXTextView: TextView? = null
    private var deltaYTextView: TextView? = null
    private var timestampTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectedTextView = findViewById(R.id.connected_status_textview)
        buttonFeedback = findViewById(R.id.last_button_textview)
        deltaXTextView = findViewById(R.id.delta_x_textview)
        deltaYTextView = findViewById(R.id.delta_y_textview)
        timestampTextView = findViewById(R.id.timestamp_textview)

        SPenRemote.connect(this, object : SPenRemote.ConnectionResultCallback {
            override fun onSuccess(unitManager: SPenUnitManager) {
                connectedTextView?.text = "true"
                val unit = unitManager.getUnit(SPenUnitType.TYPE_AIR_MOTION)
                unitManager.registerSPenEventListener(unit, airListener)
                val buttonUnit = unitManager.getUnit(SPenUnitType.TYPE_BUTTON)
                unitManager.registerSPenEventListener(buttonUnit, buttonListener)
            }

            override fun onFailure(code: SPenRemote.ConnectionResultCallback.Error) {
                connectedTextView?.text = "false"
            }

        })
    }

    private val buttonListener = SPenEventListener { event ->
        val buttonEvent = ButtonEvent(event)
        val text = when (buttonEvent.action) {
            ButtonEvent.ButtonAction.ACTION_UP -> "Button up"
            ButtonEvent.ButtonAction.ACTION_DOWN -> "Button down"
        }
        runOnUiThread {
            buttonFeedback?.text = text
        }
    }

    private val airListener = SPenEventListener { event ->
        val event = AirMotionEvent(event)
        runOnUiThread {
            deltaXTextView?.text = event.deltaX.toString()
            deltaYTextView?.text = event.deltaY.toString()
            timestampTextView?.text = event.timeStamp.toString()
        }
    }
}