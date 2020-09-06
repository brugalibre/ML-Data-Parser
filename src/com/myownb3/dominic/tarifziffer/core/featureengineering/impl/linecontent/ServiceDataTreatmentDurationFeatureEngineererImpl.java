package com.myownb3.dominic.tarifziffer.core.featureengineering.impl.linecontent;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_DATA_DURATION;

import java.util.List;
import java.util.function.Predicate;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.model.DateMutableInvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.AbstractFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent.LineContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class ServiceDataTreatmentDurationFeatureEngineererImpl extends AbstractFeatureEngineerer implements LineContentFeatureEngineerer {

   @Override
   public List<InvoiceAttr> doFeatureIngeneering(List<InvoiceAttr> invoiceAttrs, XMLFileParseResult xmlFileParseResult) {

      DateMutableInvoiceAttr treatmentBeginAttr =
            (DateMutableInvoiceAttr) findAttribute4Name(invoiceAttrs, InvoiceXMLConstants.RECORD_TARMET_TREATMENT_DATA_BEGIN);
      DateMutableInvoiceAttr treatmentEndAttr =
            (DateMutableInvoiceAttr) findAttribute4Name(invoiceAttrs, InvoiceXMLConstants.RECORD_TARMET_TREATMENT_DATA_END);

      int duration = treatmentBeginAttr.calcDaysBetween(treatmentEndAttr);
      InvoiceAttr treatmentDurationAttr = InvoiceAttrs.buildInvoiceAttr(TREATMENT_DATA_DURATION, String.valueOf(duration));
      logFeatureEngineering(treatmentDurationAttr, duration);
      return getSorted(invoiceAttrs, treatmentDurationAttr);
   }

   @Override
   public boolean has2EngineerInvoiceAttrs(List<InvoiceAttr> invoiceAttrs) {
      return invoiceAttrs.stream()
            .anyMatch(hasTreatmentDateEndAttr());
   }

   private Predicate<InvoiceAttr> hasTreatmentDateEndAttr() {
      return isAttribute4Name(InvoiceXMLConstants.RECORD_TARMET_TREATMENT_DATA_END);
   }
}
