/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.unfold.util

import android.content.Context
import android.hardware.devicestate.DeviceState
import android.hardware.devicestate.DeviceState.PROPERTY_FOLDABLE_DISPLAY_CONFIGURATION_INNER_PRIMARY
import android.hardware.devicestate.DeviceState.PROPERTY_FOLDABLE_DISPLAY_CONFIGURATION_OUTER_PRIMARY
import android.hardware.devicestate.DeviceStateManager
import android.hardware.devicestate.feature.flags.Flags as DeviceStateManagerFlags
import org.junit.Assume.assumeTrue

object FoldableTestUtils {
    /** Finds device state for folded and unfolded. */
    fun findDeviceStates(context: Context): FoldableDeviceStates {
        if (DeviceStateManagerFlags.deviceStatePropertyMigration()) {
            val deviceStateManager = context.getSystemService(DeviceStateManager::class.java)
            val deviceStateList = deviceStateManager.supportedDeviceStates

            val folded =
                deviceStateList.firstOrNull { state ->
                    state.hasProperty(PROPERTY_FOLDABLE_DISPLAY_CONFIGURATION_OUTER_PRIMARY)
                }
            val unfolded =
                deviceStateList.firstOrNull { state ->
                    state.hasProperty(PROPERTY_FOLDABLE_DISPLAY_CONFIGURATION_INNER_PRIMARY)
                }

            assumeTrue(
                "Test should only be ran on a foldable device",
                (folded != null) && (unfolded != null)
            )

            return FoldableDeviceStates(folded = folded!!, unfolded = unfolded!!)
        } else {
            val foldedDeviceStates: IntArray =
                context.resources.getIntArray(
                    com.android.internal.R.array.config_foldedDeviceStates
                )
            assumeTrue(
                "Test should be launched on a foldable device",
                foldedDeviceStates.isNotEmpty()
            )

            val folded = getDeviceState(identifier = foldedDeviceStates.maxOrNull()!!)
            val unfolded = getDeviceState(identifier = folded.identifier + 1)
            return FoldableDeviceStates(folded = folded, unfolded = unfolded)
        }
    }

    private fun getDeviceState(identifier: Int): DeviceState {
        return DeviceState(DeviceState.Configuration.Builder(identifier, "" /* name */).build())
    }
}

data class FoldableDeviceStates(val folded: DeviceState, val unfolded: DeviceState)
