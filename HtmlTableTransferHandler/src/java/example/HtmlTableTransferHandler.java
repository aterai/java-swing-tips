package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:homepage@
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.Date;
import java.util.Objects;
import javax.swing.*;

public class HtmlTableTransferHandler extends TransferHandler {
    protected final boolean canStartDrag(JComponent c) {
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            return table.getRowSelectionAllowed() || table.getColumnSelectionAllowed();
        }
        return false;
    }
    private static int[] getSelectedRows(JTable table) {
        int[] rows;
        if (table.getRowSelectionAllowed()) {
            rows = table.getSelectedRows();
        } else {
            int rowCount = table.getRowCount();
            rows = new int[rowCount];
            for (int counter = 0; counter < rowCount; counter++) {
                rows[counter] = counter;
            }
        }
        return rows;
    }
    private static int[] getSelectedColumns(JTable table) {
        int[] cols;
        if (table.getColumnSelectionAllowed()) {
            cols = table.getSelectedColumns();
        } else {
            int colCount = table.getColumnCount();
            cols = new int[colCount];
            for (int counter = 0; counter < colCount; counter++) {
                cols[counter] = counter;
            }
        }
        return cols;
    }
    protected final void appendTag(StringBuilder htmlBuf, Object obj) {
        if (obj instanceof Date) {
            String v = Objects.toString((Date) obj, "");
            htmlBuf.append("  <td><time>" + v + "</time></td>\n");
        } else if (obj instanceof Color) {
            htmlBuf.append(String.format("  <td style='background-color:#%06X'>&nbsp;</td>%n", ((Color) obj).getRGB() & 0xFFFFFF));
        } else {
            htmlBuf.append("  <td>" + Objects.toString(obj, "") + "</td>\n");
        }
    }
    // @see javax/swing/plaf/basic/BasicTableUI.TableTransferHandler#createTransferable(JComponent)
    @Override protected Transferable createTransferable(JComponent c) {
        if (canStartDrag(c)) {
            JTable table = (JTable) c;
            int[] rows = getSelectedRows(table);
            int[] cols = getSelectedColumns(table);
            // if (Objects.isNull(rows) || Objects.isNull(cols) || rows.length == 0 || cols.length == 0) {
            if (rows.length == 0 || cols.length == 0) {
                return null;
            }

            StringBuilder plainBuf = new StringBuilder();
            StringBuilder htmlBuf = new StringBuilder(64);
            htmlBuf.append("<html>\n<body>\n<table border='1'>\n");
            for (int row: rows) {
                htmlBuf.append("<tr>\n");
                for (int col: cols) {
                    Object obj = table.getValueAt(row, col);
                    String val = Objects.toString(obj, "") + "\t";
                    plainBuf.append(val);
                    appendTag(htmlBuf, obj);
                }
                // we want a newline at the end of each line and not a tab
                plainBuf.deleteCharAt(plainBuf.length() - 1).append("\n");
                htmlBuf.append("</tr>\n");
            }

            // remove the last newline
            plainBuf.deleteCharAt(plainBuf.length() - 1);
            htmlBuf.append("</table>\n</body>\n</html>");

            return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
        }
        return null;
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
}
