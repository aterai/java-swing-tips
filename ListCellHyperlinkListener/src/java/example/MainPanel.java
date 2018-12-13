// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    DefaultListModel<SiteItem> m = new DefaultListModel<>();
    m.addElement(new SiteItem("aterai", Arrays.asList("https://ateraimemo.com", "https://github.com/aterai")));
    m.addElement(new SiteItem("example", Arrays.asList("http://www.example.com", "https://www.example.com")));

    JList<SiteItem> list = new JList<>(m);
    list.setFixedCellHeight(120);
    list.setCellRenderer(new SiteListItemRenderer());
    list.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
          SiteItem item = list.getModel().getElementAt(index);
          Component c = list.getCellRenderer().getListCellRendererComponent(list, item, index, false, false);
          if (c instanceof JEditorPane) {
            Rectangle r = list.getCellBounds(index, index);
            c.setBounds(r);
            MouseEvent me = SwingUtilities.convertMouseEvent(list, e, c);
            me.translatePoint(pt.x - r.x - me.getX(), pt.y - r.y - me.getY());
            c.dispatchEvent(me);
            // TEST1:
            // c.dispatchEvent(SwingUtilities.convertMouseEvent(list, e, c));
            // TEST2:
            // pt.translate(-r.x, -r.y);
            // c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(),
            //   pt.x, pt.y, e.getClickCount(), e.isPopupTrigger()));
          }
        }
      }
    });

    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SiteItem {
  public final String name;
  public final List<String> link;

  protected SiteItem(String name, List<String> link) {
    this.name = name;
    this.link = link;
  }
}

class SiteListItemRenderer extends JEditorPane implements ListCellRenderer<SiteItem> {
  protected SiteListItemRenderer() {
    super();
    this.setContentType("text/html");
    this.setEditable(false);
    this.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        Toolkit.getDefaultToolkit().beep();
        System.out.println("You click the link with the URL " + e.getURL());
      }
    });
  }

  @Override public Component getListCellRendererComponent(JList<? extends SiteItem> list, SiteItem item, int index, boolean isSelected, boolean cellHasFocus) {
    StringBuilder buf = new StringBuilder(100);
    buf.append("<html><h1>" + item.name + "</h1><table>");
    for (int c = 0; c < item.link.size(); c++) {
      String url = item.link.get(c);
      buf.append("<tr><td><a href='" + url + "'>" + url + "</a></td></tr>");
    }
    buf.append("</table></html>");
    this.setText(buf.toString());
    this.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
    return this;
  }
}
