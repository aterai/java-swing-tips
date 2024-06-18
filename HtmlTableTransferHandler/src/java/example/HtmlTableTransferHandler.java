// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.Date;
import java.util.Objects;
import javax.swing.*;

public final class HtmlTableTransferHandler extends TransferHandler {
  public boolean canStartDrag(JTable table) {
    return table.getRowSelectionAllowed() || table.getColumnSelectionAllowed();
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

  public void appendTag(StringBuilder htmlBuf, Object obj) {
    if (obj instanceof Date) {
      String v = Objects.toString(obj, "");
      htmlBuf.append("  <td><time>").append(v).append("</time></td>");
    } else if (obj instanceof Color) {
      int rgb = ((Color) obj).getRGB() & 0xFF_FF_FF;
      htmlBuf.append(String.format("  <td style='background-color:#%06X'>&nbsp;</td>%n", rgb));
    } else {
      htmlBuf.append("  <td>").append(Objects.toString(obj, "")).append("</td>");
    }
  }

  // @see javax/swing/plaf/basic/BasicTableUI.TableTransferHandler#createTransferable(JComponent)
  @Override protected Transferable createTransferable(JComponent c) {
    Transferable transferable = null;
    if (c instanceof JTable && canStartDrag((JTable) c)) {
      JTable table = (JTable) c;
      int[] rows = getSelectedRows(table);
      int[] cols = getSelectedColumns(table);
      // if (rows == null || cols == null || rows.length == 0 || cols.length == 0) {
      if (rows.length > 0 && cols.length > 0) {
        StringBuilder plainBuf = new StringBuilder();
        StringBuilder htmlBuf = new StringBuilder(1024);
        htmlBuf.append("<html><body><table border='1'>");
        for (int row : rows) {
          htmlBuf.append("<tr>");
          for (int col : cols) {
            Object obj = table.getValueAt(row, col);
            String val = Objects.toString(obj, "") + "\t";
            plainBuf.append(val);
            appendTag(htmlBuf, obj);
          }
          // we want a newline at the end of each line and not a tab
          plainBuf.deleteCharAt(plainBuf.length() - 1).append('\n');
          htmlBuf.append("</tr>");
        }

        // remove the last newline
        plainBuf.deleteCharAt(plainBuf.length() - 1);
        htmlBuf.append("</table></body></html>");

        transferable = new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
      }
    }
    return transferable;
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY;
  }
}
