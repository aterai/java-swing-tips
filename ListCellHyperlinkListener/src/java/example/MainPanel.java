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
import javax.swing.event.HyperlinkListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<SiteItem> m = new DefaultListModel<>();
    List<String> link1 = Arrays.asList("https://ateraimemo.com", "https://github.com/aterai");
    m.addElement(new SiteItem("aterai", link1));
    List<String> link2 = Arrays.asList("http://www.example.com", "https://www.example.com");
    m.addElement(new SiteItem("example", link2));

    JList<SiteItem> list = new JList<SiteItem>(m) {
      @Override public void updateUI() {
        super.updateUI();
        setFixedCellHeight(120);
        setCellRenderer(new SiteListItemRenderer());
      }
    };
    list.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        Point pt = e.getPoint();
        int idx = list.locationToIndex(pt);
        if (idx >= 0) {
          SiteItem item = list.getModel().getElementAt(idx);
          ListCellRenderer<? super SiteItem> renderer = list.getCellRenderer();
          Component c = renderer.getListCellRendererComponent(list, item, idx, false, false);
          if (c instanceof JEditorPane) {
            Rectangle r = list.getCellBounds(idx, idx);
            c.setBounds(r);
            MouseEvent me = SwingUtilities.convertMouseEvent(list, e, c);
            me.translatePoint(pt.x - r.x - me.getX(), pt.y - r.y - me.getY());
            c.dispatchEvent(me);
            // TEST1:
            // c.dispatchEvent(SwingUtilities.convertMouseEvent(list, e, c));
            // TEST2:
            // pt.translate(-r.x, -r.y);
            // int modifiers = e.getModifiers() | e.getModifiersEx();
            // c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), modifiers,
            //     pt.x, pt.y, e.getClickCount(), e.isPopupTrigger()));
          }
        }
      }
    });

    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
  private final String name;
  private final List<String> link;

  protected SiteItem(String name, List<String> link) {
    this.name = name;
    this.link = link;
  }

  public String getName() {
    return name;
  }

  public List<String> getLink() {
    return link;
  }
}

class SiteListItemRenderer implements ListCellRenderer<SiteItem> {
  private final JEditorPane renderer = new JEditorPane("text/html", "") {
    private transient HyperlinkListener listener;
    @Override public void updateUI() {
      removeHyperlinkListener(listener);
      super.updateUI();
      // setContentType("text/html");
      setEditable(false);
      listener = e -> {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          Component c = ((JComponent) e.getSource()).getRootPane();
          JOptionPane.showMessageDialog(c, "You click the link with the URL " + e.getURL());
        }
      };
      addHyperlinkListener(listener);
    }
  };

  @Override public Component getListCellRendererComponent(JList<? extends SiteItem> list, SiteItem item, int index, boolean isSelected, boolean cellHasFocus) {
    StringBuilder buf = new StringBuilder(100);
    buf.append("<html><h1>").append(item.getName()).append("</h1><table>");
    for (String url : item.getLink()) {
      buf.append("<tr><td><a href='").append(url).append("'>").append(url);
      // .append("</a></td></tr>");
    }
    buf.append("</table></html>");
    renderer.setText(buf.toString());
    renderer.setOpaque(true);
    renderer.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
    return renderer;
  }
}
