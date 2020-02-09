package com.snorlax.snorlax.utils.processor

import org.apache.poi.xwpf.usermodel.XWPFDocument

abstract class BaseWordProcessor(document: XWPFDocument) {
    protected val table = document.tables[0]!!
}