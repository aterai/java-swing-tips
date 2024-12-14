// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box accordion = Box.createVerticalBox();
    accordion.setOpaque(true);
    accordion.setBackground(new Color(0xB4_B4_FF));
    accordion.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
    makeExpansionPanelList().forEach(p -> {
      accordion.add(p);
      accordion.add(Box.createVerticalStrut(5));
    });
    accordion.add(Box.createVerticalGlue());

    JScrollPane scroll = new JScrollPane(accordion);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, new JPanel());
    split.setResizeWeight(.5);
    split.setDividerLocation(160);
    split.setDividerSize(2);

    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  // public void addComp(Component comp) {
  //   box.remove(glue);
  //   box.add(Box.createVerticalStrut(5));
  //   box.add(comp);
  //   box.add(glue);
  //   box.revalidate();
  // }

  private List<AbstractExpansionPanel> makeExpansionPanelList() {
    return Arrays.asList(
      new AbstractExpansionPanel("System Tasks") {
        @Override public JPanel makePanel() {
          JPanel p = new JPanel(new GridLayout(0, 1));
          Stream.of("1111", "222222")
              .map(JCheckBox::new)
              .forEach(b -> {
                b.setOpaque(false);
                p.add(b);
              });
          return p;
        }
      },
      new AbstractExpansionPanel("Other Places") {
        @Override public JPanel makePanel() {
          JPanel p = new JPanel(new GridLayout(0, 1));
          Stream.of("Desktop", "My Network Places", "My Documents", "Shared Documents")
              .map(JLabel::new)
              .forEach(p::add);
          return p;
        }
      },
      new AbstractExpansionPanel("Details") {
        @Override public JPanel makePanel() {
          JPanel p = new JPanel(new GridLayout(0, 1));
          ButtonGroup bg = new ButtonGroup();
          Stream.of("aaa", "bbb", "ccc", "ddd")
              .map(JRadioButton::new)
              .forEach(b -> {
                b.setSelected(p.getComponentCount() == 0);
                b.setOpaque(false);
                p.add(b);
                bg.add(b);
              });
          return p;
        }
      }
    );
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

abstract class AbstractExpansionPanel extends JPanel {
  private final String title;
  private final JLabel label;
  private final JPanel panel = makePanel();

  protected AbstractExpansionPanel(String title) {
    super(new BorderLayout());
    this.title = title;
    label = new JLabel("▼ " + title) {
      private final Color bgc = new Color(0xC8_C8_FF);
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Insets ins = getInsets();
        g2.setPaint(new GradientPaint(50f, 0f, Color.WHITE, getWidth(), getHeight(), bgc));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        initPanel();
      }
    });
    label.setForeground(Color.BLUE);
    label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
    add(label, BorderLayout.NORTH);

    panel.setVisible(false);
    panel.setOpaque(true);
    panel.setBackground(new Color(0xF0_F0_FF));
    Border outBorder = BorderFactory.createMatteBorder(0, 2, 2, 2, Color.WHITE);
    Border inBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    Border border = BorderFactory.createCompoundBorder(outBorder, inBorder);
    panel.setBorder(border);
    add(panel);
  }

  public abstract JPanel makePanel();

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = label.getPreferredSize();
    if (panel.isVisible()) {
      d.height += panel.getPreferredSize().height;
    }
    return d;
  }

  @Override public Dimension getMaximumSize() {
    Dimension d = getPreferredSize();
    d.width = Short.MAX_VALUE;
    return d;
  }

  protected void initPanel() {
    panel.setVisible(!panel.isVisible());
    label.setText(String.format("%s %s", panel.isVisible() ? "△" : "▼", title));
    revalidate();
    // fireExpansionEvent();
    EventQueue.invokeLater(() -> panel.scrollRectToVisible(panel.getBounds()));
  }

  // protected Vector<ExpansionListener> expansionListenerList = new Vector<>();

  // public void addExpansionListener(ExpansionListener listener) {
  //   if (!expansionListenerList.contains(listener)) {
  //     expansionListenerList.add(listener);
  //   }
  // }

  // public void removeExpansionListener(ExpansionListener listener) {
  //   expansionListenerList.remove(listener);
  // }

  // public void fireExpansionEvent() {
  //   Vector list = (Vector) expansionListenerList.clone();
  //   Enumeration enm = list.elements();
  //   ExpansionEvent e = new ExpansionEvent(this);
  //   while (enm.hasMoreElements()) {
  //     ExpansionListener listener = (ExpansionListener) enm.nextElement();
  //     listener.expansionStateChanged(e);
  //   }
  // }
}

// class ExpansionEvent extends EventObject {
//   protected ExpansionEvent(Object source) {
//     super(source);
//   }
// }
//
// interface ExpansionListener {
//   void expansionStateChanged(ExpansionEvent e);
// }
