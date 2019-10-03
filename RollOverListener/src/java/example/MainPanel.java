// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("Name1-comment");
    model.addElement("Name2-test");
    model.addElement("11111111111");
    model.addElement("35663456345634563456");
    model.addElement("222222222222222222");
    model.addElement("Name0-333333333");
    model.addElement("44444444444444444444");
    model.addElement("5555555555555555");
    model.addElement("66666666666666666666666");
    model.addElement("4352345123452345234523452345234534");

    JList<String> list = new RollOverList<>(model);
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class RollOverList<E> extends JList<E> {
  private transient RollOverCellHandler rollOverHandler;

  protected RollOverList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseListener(rollOverHandler);
    removeMouseMotionListener(rollOverHandler);
    setSelectionBackground(null); // Nimbus
    super.updateUI();
    rollOverHandler = new RollOverCellHandler();
    addMouseMotionListener(rollOverHandler);
    addMouseListener(rollOverHandler);
    setCellRenderer(rollOverHandler);
  }

  private class RollOverCellHandler extends MouseAdapter implements ListCellRenderer<E> {
    private final Color rolloverBackground = new Color(0xDC_F0_FF);
    private int rollOverRowIndex = -1;
    private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (index == rollOverRowIndex) {
        c.setBackground(rolloverBackground);
        if (isSelected) {
          c.setForeground(Color.BLACK);
        }
        // c.setForeground(getSelectionForeground());
        // c.setBackground(getSelectionBackground());
      }
      return c;
    }

    @Override public void mouseExited(MouseEvent e) {
      rollOverRowIndex = -1;
      repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {
      int row = locationToIndex(e.getPoint());
      if (row != rollOverRowIndex) {
        Rectangle rect = getCellBounds(row, row);
        if (rollOverRowIndex >= 0) {
          rect.add(getCellBounds(rollOverRowIndex, rollOverRowIndex));
        }
        rollOverRowIndex = row;
        repaint(rect);
      }
    }
  }
}
