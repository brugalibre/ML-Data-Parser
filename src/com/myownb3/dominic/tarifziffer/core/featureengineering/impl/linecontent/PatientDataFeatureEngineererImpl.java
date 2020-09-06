package com.myownb3.dominic.tarifziffer.core.featureengineering.impl.linecontent;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_AGE;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_BIRTHDATE;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_DATA_BEGIN;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_SUFFIX;

import java.util.List;
import java.util.function.Predicate;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.model.DateMutableInvoiceAttr;
import com.myownb3.dominic.invoice.attrs.model.IntegerMutableInvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.export.ContentUtil;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.AbstractFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent.LineContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class PatientDataFeatureEngineererImpl extends AbstractFeatureEngineerer implements LineContentFeatureEngineerer {

   @Override
   public List<InvoiceAttr> doFeatureIngeneering(List<InvoiceAttr> invoiceAttrs, XMLFileParseResult xmlFileParseResult) {
      DateMutableInvoiceAttr treatmentBeginAttr = getTreatmentBeginAttr(xmlFileParseResult);
      DateMutableInvoiceAttr birthdateAttr = findAttributeByName(invoiceAttrs, PATIENT_DATA_BIRTHDATE, DateMutableInvoiceAttr.class);
      int ageAsInt = calculateAge(birthdateAttr, treatmentBeginAttr);

      IntegerMutableInvoiceAttr ageInvoiceAttr = findAttributeByName(invoiceAttrs, PATIENT_DATA_AGE, IntegerMutableInvoiceAttr.class);
      ageInvoiceAttr.setValue(ageAsInt);
      logFeatureEngineering(xmlFileParseResult.getXMLFileName(), ageInvoiceAttr, ageAsInt);
      return invoiceAttrs;
   }

   @Override
   public boolean has2EngineerInvoiceAttrs(List<InvoiceAttr> invoiceAttrs) {
      return invoiceAttrs.stream()
            .anyMatch(isBirthdateAttribute());
   }

   private DateMutableInvoiceAttr getTreatmentBeginAttr(XMLFileParseResult xmlFileParseResult) {
      List<InvoiceAttr> allInvoiceAttrs4File = ContentUtil.getAllInvoiceAttrs4File(xmlFileParseResult);
      return findAttributeByName(allInvoiceAttrs4File, TREATMENT_SUFFIX + TREATMENT_DATA_BEGIN, DateMutableInvoiceAttr.class);
   }

   private Predicate<InvoiceAttr> isBirthdateAttribute() {
      return isAttribute4Name(PATIENT_DATA_BIRTHDATE);
   }

   private static int calculateAge(DateMutableInvoiceAttr birthdateAttr, DateMutableInvoiceAttr treatmentBeginAttr) {
      return birthdateAttr.calcDaysBetween(treatmentBeginAttr) / 365;
   }
}
