package com.myownb3.dominic.tarifziffer.core.export;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

/**
 * Contains utility methods for dealing the {@link LineContent}
 * 
 * @author Dominic
 *
 */
public class ContentUtil {

   private ContentUtil() {
      // private 
   }

   /**
    * Return all {@link InvoiceAttr}s for the given {@link XMLFileParseResult}<
    * 
    * @param xmlFileParseResult
    * @return all {@link InvoiceAttr}s for the given {@link XMLFileParseResult}<
    */
   public static List<InvoiceAttr> getAllInvoiceAttrs4File(XMLFileParseResult xmlFileParseResult) {
      return xmlFileParseResult.getContent()
            .stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .collect(Collectors.toList());
   }

   /**
    * Return all {@link LineContent} which are services content
    * 
    * @see LineContent#isServicesContent()
    * @param xmlFileParseResult
    *        the {@link XMLFileParseResult}
    * @return all {@link LineContent} which are services content
    */
   public static List<LineContent> getServicesLineContent(XMLFileParseResult xmlFileParseResult) {
      return filterContent4Type(xmlFileParseResult.getContent(), LineContent::isServicesContent);
   }

   /**
    * Return all {@link LineContent} which are services content
    * 
    * @see LineContent#isServicesContent()
    * @param lineContents
    *        the {@link LineContent} to check
    * @return all {@link LineContent} which are services content
    */
   public static List<LineContent> getServiceLineContent(List<LineContent> lineContents) {
      return filterContent4Type(lineContents, LineContent::isServicesContent);
   }

   /**
    * Return all {@link LineContent} which are retrieved from the header of an invoice.
    * Those values are by default <b>not</b> services content
    * 
    * @see LineContent#isServicesContent()
    * @param xmlFileParseResult
    *        the {@link XMLFileParseResult}
    * @return all {@link LineContent} which are header content
    */
   public static List<LineContent> getInvoiceHeaderContent(XMLFileParseResult xmlFileParseResult) {
      return getInvoiceHeaderContent(xmlFileParseResult.getContent());
   }

   /**
    * Return all {@link LineContent} which are retrieved from the header of an invoice.
    * Those values are by default <b>not</b> services content
    * 
    * @see LineContent#isServicesContent()
    * @param lineContents
    *        the {@link LineContent} to check
    * @return all {@link LineContent} which are services content
    */
   public static List<LineContent> getInvoiceHeaderContent(List<LineContent> lineContents) {
      Predicate<LineContent> isServiceContent = LineContent::isServicesContent;
      return lineContents.stream()
            .filter(isServiceContent.negate())
            .sorted(Comparator.comparing(LineContent::getContentType))
            .collect(Collectors.toList());
   }

   private static List<LineContent> filterContent4Type(List<LineContent> lineContents, Predicate<LineContent> isContentType) {
      return lineContents.parallelStream()
            .filter(isContentType)
            .collect(Collectors.toList());
   }
}
