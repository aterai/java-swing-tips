package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// Copied from javax/swing/plaf/basic/BasicTransferable.java
class BasicTransferable implements Transferable {
    protected String plainData;
    protected String htmlData;
    private static DataFlavor[] htmlFlavors;
    private static DataFlavor[] stringFlavors;
    private static DataFlavor[] plainFlavors;

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
            stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String");
            stringFlavors[1] = DataFlavor.stringFlavor;

        } catch (ClassNotFoundException ex) {
            System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable");
        }
    }

    protected BasicTransferable(String plainData, String htmlData) {
        this.plainData = plainData;
        this.htmlData = htmlData;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    @SuppressWarnings("LocalVariableName")
    @Override public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] richerFlavors = getRicherFlavors();
        int nRicher = richerFlavors.length; // Objects.nonNull(richerFlavors) ? richerFlavors.length : 0;
        int nHtml = isHtmlSupported() ? htmlFlavors.length : 0;
        int nPlain = isPlainSupported() ? plainFlavors.length : 0;
        int nString = isPlainSupported() ? stringFlavors.length : 0;
        int nFlavors = nRicher + nHtml + nPlain + nString;
        DataFlavor[] flavors = new DataFlavor[nFlavors];

        // fill in the array
        int nDone = 0;
        // if (nRicher > 0) {
        System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
        nDone += nRicher;
        // }
        // if (nHtml > 0) {
        System.arraycopy(htmlFlavors, 0, flavors, nDone, nHtml);
        nDone += nHtml;
        // }
        // if (nPlain > 0) {
        System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
        nDone += nPlain;
        // }
        // if (nString > 0) {
        System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
        //     nDone += nString;
        // }
        return flavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        // DataFlavor[] flavors = getTransferDataFlavors();
        // for (int i = 0; i < flavors.length; i++) {
        //     if (flavors[i].equals(flavor)) {
        //         return true;
        //     }
        // }
        // return false;
        return Stream.of(getTransferDataFlavors()).anyMatch(f -> f.equals(flavor));
    }

    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                if the data is no longer available
     *              in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     *              not supported.
     */
    @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        // ???: DataFlavor[] richerFlavors = getRicherFlavors();
        if (isRicherFlavor(flavor)) {
            return getRicherData(flavor);
        } else if (isHtmlFlavor(flavor)) {
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
        //     return null;
        // }
        // String encoding = flavor.getParameter("charset");
        // return Objects.nonNull(encoding) ? encoding : Charset.defaultCharset().name();
        return Optional.ofNullable(flavor.getParameter("charset")).orElse(Charset.defaultCharset().name());
    }
    private InputStream createInputStream(DataFlavor flavor, String data) throws IOException, UnsupportedFlavorException {
        String cs = Optional.ofNullable(getTextCharset(flavor)).orElseThrow(() -> new UnsupportedFlavorException(flavor));
        return new ByteArrayInputStream(data.getBytes(cs));
    }

    // --- richer subclass flavors ----------------------------------------------

    protected boolean isRicherFlavor(DataFlavor flavor) {
        // DataFlavor[] richerFlavors = getRicherFlavors();
        // int nFlavors = richerFlavors.length; // Objects.nonNull(richerFlavors) ? richerFlavors.length : 0;
        // for (int i = 0; i < nFlavors; i++) {
        //     if (richerFlavors[i].equals(flavor)) {
        //         return true;
        //     }
        // }
        // return false;
        return Stream.of(getRicherFlavors()).anyMatch(f -> f.equals(flavor));
    }

    /**
     * Some subclasses will have flavors that are more descriptive than HTML
     * or plain text.  If this method returns a non-null value, it will be
     * placed at the start of the array of supported flavors.
     */
    protected DataFlavor[] getRicherFlavors() {
        return new DataFlavor[0]; // null;
    }

    protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
        return null;
    }

    // --- html flavors ----------------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is an HTML flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isHtmlFlavor(DataFlavor flavor) {
        // DataFlavor[] flavors = htmlFlavors;
        // for (int i = 0; i < flavors.length; i++) {
        //     if (flavors[i].equals(flavor)) {
        //         return true;
        //     }
        // }
        // return false;
        return Stream.of(htmlFlavors).anyMatch(f -> f.equals(flavor));
    }

    /**
     * Should the HTML flavors be offered?  If so, the method
     * getHtmlData should be implemented to provide something reasonable.
     */
    protected boolean isHtmlSupported() {
        return Objects.nonNull(htmlData);
    }

    /**
     * Fetch the data in a text/html format.
     */
    protected String getHtmlData() {
        return htmlData;
    }

    protected Object getHtmlTransferData(DataFlavor flavor) throws IOException, UnsupportedFlavorException {
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
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isPlainFlavor(DataFlavor flavor) {
        // DataFlavor[] flavors = plainFlavors;
        // for (int i = 0; i < flavors.length; i++) {
        //     if (flavors[i].equals(flavor)) {
        //         return true;
        //     }
        // }
        // return false;
        return Stream.of(plainFlavors).anyMatch(f -> f.equals(flavor));
    }

    /**
     * Should the plain text flavors be offered?  If so, the method
     * getPlainData should be implemented to provide something reasonable.
     */
    protected boolean isPlainSupported() {
        return Objects.nonNull(plainData);
    }

    /**
     * Fetch the data in a text/plain format.
     */
    protected String getPlainData() {
        return plainData;
    }

    protected Object getPlaneTransferData(DataFlavor flavor) throws IOException, UnsupportedFlavorException {
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

    // --- string flavorss --------------------------------------------------------

    /**
     * Returns whether or not the specified data flavor is a String flavor that
     * is supported.
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    protected boolean isStringFlavor(DataFlavor flavor) {
        // DataFlavor[] flavors = stringFlavors;
        // for (int i = 0; i < flavors.length; i++) {
        //     if (flavors[i].equals(flavor)) {
        //         return true;
        //     }
        // }
        // return false;
        return Stream.of(stringFlavors).anyMatch(f -> f.equals(flavor));
    }
}
