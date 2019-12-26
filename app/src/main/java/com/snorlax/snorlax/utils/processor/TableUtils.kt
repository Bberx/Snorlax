/*
 * Copyright 2019 Oliver Rhyme G. Añasco
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.snorlax.snorlax.utils.processor

import io.reactivex.Completable
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.apache.poi.xwpf.usermodel.XWPFTableRow

object TableUtils {

    fun deleteColumn(table: XWPFTable, pos: Int) : Completable {
        return Completable.fromAction {
            var maxColumn = 0
            for (i in 0..table.rows.lastIndex) {
                val row: XWPFTableRow = table.rows[i]

                val lastColumn = row.tableCells.lastIndex
                if (lastColumn > maxColumn) maxColumn = lastColumn
                if (lastColumn < pos) continue

                for (j in pos..lastColumn) {
                    val oldCell: XWPFTableCell? = row.tableCells[j]
                    oldCell?.let {
                        row.removeCell(j)
                    }

                }
            }
        }
    }
}