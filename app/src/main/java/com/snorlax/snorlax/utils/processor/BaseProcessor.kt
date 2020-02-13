package com.snorlax.snorlax.utils.processor

import org.apache.poi.xwpf.usermodel.XWPFDocument

abstract class BaseProcessor(protected val document: XWPFDocument) {
    protected val table = document.tables[0]!!
}