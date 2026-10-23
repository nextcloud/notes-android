/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package it.niedermann.owncloud.notes.util

import java.util.Locale

object HumanReadableFormatter {

    private const val BITS_PER_UNIT = 10

    @JvmStatic
    fun formatBytes(bytes: Long): String? {
        if (bytes < 0) {
            return null
        }

        val highestSetBit = Long.SIZE_BITS - 1 - bytes.countLeadingZeroBits()
        val unitIndex = (highestSetBit / BITS_PER_UNIT).coerceIn(0, ByteUnit.entries.lastIndex)
        val unit = ByteUnit.entries[unitIndex]
        val size = bytes.toDouble() / (1L shl (BITS_PER_UNIT * unitIndex))

        val pattern = "%.${unit.decimals}f"
        val formattedSize = String.format(Locale.ROOT, pattern, size)
        return "$formattedSize ${unit.suffix}"
    }

    enum class ByteUnit(val suffix: String, val decimals: Int) {
        BYTES("B", 0),
        KILOBYTES("KB", 0),
        MEGABYTES("MB", 1),
        GIGABYTES("GB", 1),
        TERABYTES("TB", 1),
        PETABYTES("PB", 2),
        EXABYTES("EB", 2)
    }

}
