package org.maiaframework.job.batch.csv

import org.supercsv.cellprocessor.CellProcessorAdaptor
import org.supercsv.util.CsvContext

class TrimmingCellProcessor: CellProcessorAdaptor() {


    override fun <T : Any?> execute(value: Any?, context: CsvContext): T {

        if (value is String) {
            return value.trim { it <= ' ' || it >= '\uFEFF'} as T
        } else {
            return value as T
        }

    }


    companion object {

        val INSTANCE = TrimmingCellProcessor()

    }

}
