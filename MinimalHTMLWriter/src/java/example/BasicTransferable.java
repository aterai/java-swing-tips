// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.UIManager;

// Copied from javax/swing/plaf/basic/BasicTransferable.java
public class BasicTransferable implements Transferable {
  private static DataFlavor[] htmlFlavors;
  private static DataFlavor[] stringFlavors;
  private static DataFlavor[] plainFlavors;
  private final String plainData;
  private final String htmlData;

  static {
    try {
      htmlFlavors = new DataFlavor[3];
      htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
      htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
      htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");

      plainFlavors = new DataFlavor[3];
      plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String");
      plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
      plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");

      stringFlavors = new DataFlavor[2];
      String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String";
      stringFlavors[0] = new DataFlavor(mimeType);
      stringFlavors[1] = DataFlavor.stringFlavor;
    } catch (ClassNotFoundException ex) {
      // System.err.println("error initializing javax.swing.plaf.basic.BasicTransferable");
      UIManager.getLookAndFeel().provideErrorFeedback(null);
      ex.printStackTrace();
    }
  }

  public BasicTransferable(String plainData, String htmlData) {
    this.plainData = plainData;
    this.htmlData = htmlData;
  }

  /**
   * Returns an array of DataFlavor objects indicating the flavors the data
   * can be provided in.  The array should be ordered according to preference
   * for providing the data (from most richly descriptive to least descriptive).
   *
   * @return an array of data flavors in which this data can be transferred
   */
  @Override public DataFlavor[] getTransferDataFlavors() {
    // DataFlavor[] richerFlavors = getRicherFlavors();
    // int numRicher = richerFlavors.length;
    int numHtml = isHtmlSupported() ? htmlFlavors.length : 0;
    int numPlain = isPlainSupported() ? plainFlavors.length : 0;
    int numString = isPlainSupported() ? stringFlavors.length : 0;
    int numFlavors = numHtml + numPlain + numString; // + numRicher;
    DataFlavor[] flavors = new DataFlavor[numFlavors];

    // fill in the array
    int numDone = 0;
    // if (numRicher > 0) {
    //  System.arraycopy(richerFlavors, 0, flavors, numDone, numRicher);
    //  numDone += numRicher;
    // }
    // if (numHtml > 0) {
    System.arraycopy(htmlFlavors, 0, flavors, numDone, numHtml);
    numDone += numHtml;
    // }
    // if (numPlain > 0) {
    System.arraycopy(plainFlavors, 0, flavors, numDone, numPlain);
    numDone += numPlain;
    // }
    // if (numString > 0) {
    System.arraycopy(stringFlavors, 0, flavors, numDone, numString);
    //   numDone += numString;
    // }
    return flavors;
  }

  /**
   * Returns whether or not the specified data flavor is supported for
   * this object.
   *
   * @param flavor  the requested flavor for the data
   * @return boolean indicating whether or not the data flavor is supported
   */
  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return Arrays.asList(getTransferDataFlavors()).contains(flavor);
  }

  /**
   * Returns an object which represents the data to be transferred.  The class
   * of the object returned is defined by the representation class of the flavor.
   *
   * @param flavor  the requested flavor for the data
   * @exception IOException  if the data is no longer available in the requested flavor.
   * @exception UnsupportedFlavorException  if the requested data flavor is not supported.
   * @see DataFlavor#getRepresentationClass
   */
  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    // DataFlavor[] richerFlavors = getRicherFlavors();
    // if (isRicherFlavor(flavor)) {
    //   return getRicherData(flavor);
    // } else
    if (isHtmlFlavor(flavor)) {
      return getHtmlTransferData(flavor);
    } else if (isPlainFlavor(flavor)) {
      return getPlaneTransferData(flavor);
    } else if (isStringFlavor(flavor)) {
      return Objects.toString(getPlainData(), "");
    }
    throw new UnsupportedFlavorException(flavor);
  }

  // @see sun.datatransfer.DataFlavorUtil
  public static String getTextCharset(DataFlavor flavor) {
    // if (!isFlavorCharsetTextType(flavor)) {
    //   return null;
    // }
    // String encoding = flavor.getParameter("charset");
    // return Objects.nonNull(encoding) ? encoding : Charset.defaultCharset().name();
    return Optional.ofNullable(flavor.getParameter("charset"))
        .orElseGet(() -> Charset.defaultCharset().name());
  }

  private InputStream createInputStream(DataFlavor flavor, String data)
      throws IOException, UnsupportedFlavorException {
    String s = getTextCharset(flavor);
    String cs = Optional.ofNullable(s).orElseThrow(() -> new UnsupportedFlavorException(flavor));
    return new ByteArrayInputStream(data.getBytes(cs));
  }

  // --- richer subclass flavors ----------------------------------------------

  // private boolean isRicherFlavor(DataFlavor flavor) {
  //   DataFlavor[] richerFlavors = getRicherFlavors();
  //   int numFlavors = richerFlavors.length;
  //   for (int i = 0; i < numFlavors; i++) {
  //     if (richerFlavors[i].equals(flavor)) {
  //       return true;
  //     }
  //   }
  //   return false;
  // }

  // /**
  //  * Some subclasses will have flavors that are more descriptive than HTML
  //  * or plain text.  If this method returns a non-null value, it will be
  //  * placed at the start of the array of supported flavors.
  //  */
  // private DataFlavor[] getRicherFlavors() {
  //   return new DataFlavor[0]; // null;
  // }

  // private Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
  //   return null;
  // }

  // --- html flavors ----------------------------------------------------------

  /**
   * Returns whether or not the specified data flavor is an HTML flavor that
   * is supported.
   *
   * @param flavor  the requested flavor for the data
   * @return boolean indicating whether or not the data flavor is supported
   */
  private boolean isHtmlFlavor(DataFlavor flavor) {
    return Arrays.asList(htmlFlavors).contains(flavor);
  }

  /**
   * Should the HTML flavors be offered?  If so, the method
   * getHtmlData should be implemented to provide something reasonable.
   */
  private boolean isHtmlSupported() {
    return Objects.nonNull(htmlData);
  }

  /**
   * Fetch the data in a text/html format.
   */
  private String getHtmlData() {
    return htmlData;
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private Object getHtmlTransferData(DataFlavor flavor)
      throws IOException, UnsupportedFlavorException {
    // String data = getHtmlData();
    // data = Objects.nonNull(data) ? data : "";
    String data = Objects.toString(getHtmlData(), "");
    if (String.class.equals(flavor.getRepresentationClass())) {
      return data;
    } else if (Reader.class.equals(flavor.getRepresentationClass())) {
      return new StringReader(data);
    } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
      // return new StringBufferInputStream(data);
      return createInputStream(flavor, data);
    }
    throw new UnsupportedFlavorException(flavor);
  }

  // --- plain text flavors ----------------------------------------------------

  /**
   * Returns whether or not the specified data flavor is an plain flavor that
   * is supported.
   *
   * @param flavor  the requested flavor for the data
   * @return boolean indicating whether or not the data flavor is supported
   */
  private boolean isPlainFlavor(DataFlavor flavor) {
    return Arrays.asList(plainFlavors).contains(flavor);
  }

  /**
   * Should the plain text flavors be offered?  If so, the method
   * getPlainData should be implemented to provide something reasonable.
   */
  private boolean isPlainSupported() {
    return Objects.nonNull(plainData);
  }

  /**
   * Fetch the data in a text/plain format.
   */
  private String getPlainData() {
    return plainData;
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private Object getPlaneTransferData(DataFlavor flavor)
      throws IOException, UnsupportedFlavorException {
    // String data = getPlainData();
    // data = Objects.nonNull(data) ? data : "";
    String data = Objects.toString(getPlainData(), "");
    if (String.class.equals(flavor.getRepresentationClass())) {
      return data;
    } else if (Reader.class.equals(flavor.getRepresentationClass())) {
      return new StringReader(data);
    } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
      // return new StringBufferInputStream(data);
      return createInputStream(flavor, data);
    }
    throw new UnsupportedFlavorException(flavor);
  }

  // --- string flavors --------------------------------------------------------

  /**
   * Returns whether the specified data flavor is a String flavor that
   * is supported.
   *
   * @param flavor  the requested flavor for the data
   * @return boolean indicating whether the data flavor is supported
   */
  private boolean isStringFlavor(DataFlavor flavor) {
    return Arrays.asList(stringFlavors).contains(flavor);
  }
}
