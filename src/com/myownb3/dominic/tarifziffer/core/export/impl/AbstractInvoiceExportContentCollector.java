package com.myownb3.dominic.tarifziffer.core.export.impl;

import java.util.List;

import com.myownb3.dominic.tarifziffer.core.export.InvoiceContentExportContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public abstract class AbstractInvoiceExportContentCollector implements InvoiceContentExportContentCollector {
   protected String tarifZiffer;
   protected List<XMLFileParseResult> result;
}
