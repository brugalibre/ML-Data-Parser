package com.myownb3.dominic.tarifziffer.core.export.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.export.InvoiceContentExportContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;

public abstract class AbstractInvoiceExportContentCollector implements InvoiceContentExportContentCollector {
   protected static final Logger LOG = Logger.getLogger(AbstractInvoiceExportContentCollector.class);
   protected String tarifZiffer;
   protected List<XMLFileParseResult> result;

   protected void beforeCollecting() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start collecting '" + result.size() + "' results");
   }

   protected void afterCollecting() {
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done collecting %s\n");
   }
}
