package com.myownb3.dominic.tarifziffer.core.featureengineering.impl.linecontent;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_AGE;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_BIRTHDATE;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_DATA_BEGIN;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_SUFFIX;

import java.util.List;
import java.util.function.Predicate;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.model.DateMutableInvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.AbstractFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent.LineContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class PatientDataFeatureEngineererImpl extends AbstractFeatureEngineerer implements LineContentFeatureEngineerer {

   @Override
   public List<InvoiceAttr> doFeatureIngeneering(List<InvoiceAttr> invoiceAttrs, XMLFileParseResult xmlFileParseResult) {
      DateMutableInvoiceAttr treatmentBeginAttr = getTreatmentBeginAttr(xmlFileParseResult);
      DateMutableInvoiceAttr birthdateAttr = (DateMutableInvoiceAttr) findAttribute4Name(invoiceAttrs, PATIENT_DATA_BIRTHDATE);

      int ageAsInt = calculateAge(birthdateAttr, treatmentBeginAttr);
      InvoiceAttr ageInvoiceAttr = InvoiceAttrs.buildInvoiceAttr(PATIENT_DATA_AGE, String.valueOf(ageAsInt));
      logFeatureEngineering(ageInvoiceAttr, ageInvoiceAttr);
      return getSorted(invoiceAttrs, ageInvoiceAttr);
   }

   @Override
   public boolean has2EngineerInvoiceAttrs(List<InvoiceAttr> invoiceAttrs) {
      return invoiceAttrs.stream()
            .anyMatch(isBirthdateAttribute());
   }

   protected DateMutableInvoiceAttr getTreatmentBeginAttr(XMLFileParseResult xmlFileParseResult) {
      List<InvoiceAttr> allInvoiceAttrs4File = getAllInvoiceAttrs4File(xmlFileParseResult);
      return (DateMutableInvoiceAttr) findAttribute4Name(allInvoiceAttrs4File, TREATMENT_SUFFIX + TREATMENT_DATA_BEGIN);
   }

   private Predicate<InvoiceAttr> isBirthdateAttribute() {
      return isAttribute4Name(PATIENT_DATA_BIRTHDATE);
   }

   private static int calculateAge(DateMutableInvoiceAttr birthdateAttr, DateMutableInvoiceAttr treatmentBeginAttr) {
      return birthdateAttr.calcDaysBetween(treatmentBeginAttr) / 365;
   }
}
