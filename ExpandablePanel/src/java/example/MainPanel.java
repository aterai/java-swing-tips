// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
// import javax.swing.event.EventListenerList;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Box northBox = Box.createVerticalBox();
    Box centerBox = Box.createVerticalBox();
    Box southBox = Box.createVerticalBox();
    List<? extends AbstractExpansionPanel> panelList = makeList();

    ExpansionListener rl = e -> {
      setVisible(false);
      Component source = (Component) e.getSource();
      centerBox.removeAll();
      northBox.removeAll();
      southBox.removeAll();
      boolean insertSouth = false;
      for (AbstractExpansionPanel exp: panelList) {
        if (source.equals(exp) && exp.isExpanded()) {
          centerBox.add(exp);
          insertSouth = true;
          continue;
        }
        exp.setExpanded(false);
        if (insertSouth) {
          southBox.add(exp);
        } else {
          northBox.add(exp);
        }
      }
      setVisible(true);
    };

    panelList.forEach(exp -> {
      northBox.add(exp);
      exp.addExpansionListener(rl);
    });

    JPanel panel = new JPanel(new BorderLayout()) {
      @Override public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        d.width = 120;
        return d;
      }
    };
    panel.add(northBox, BorderLayout.NORTH);
    panel.add(centerBox);
    panel.add(southBox, BorderLayout.SOUTH);

    JSplitPane sp = new JSplitPane();
    sp.setLeftComponent(panel);
    sp.setRightComponent(new JScrollPane(new JTree()));
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private List<? extends AbstractExpansionPanel> makeList() {
    return Arrays.asList(
      new AbstractExpansionPanel("Panel1") {
        @Override public Container makePanel() {
          Box p = Box.createVerticalBox();
          p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
          p.add(new JCheckBox("aaaa"));
          p.add(new JCheckBox("bbbbbbbbb"));
          return p;
        }
      },
      new AbstractExpansionPanel("Panel2") {
        @Override public Container makePanel() {
          Box p = Box.createVerticalBox();
          p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
          for (int i = 0; i < 16; i++) {
            p.add(new JLabel(String.format("%02d", i)));
          }
          return p;
        }
      },
      new AbstractExpansionPanel("Panel3") {
        @Override public Container makePanel() {
          Box p = Box.createVerticalBox();
          p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
          ButtonGroup bg = new ButtonGroup();
          Stream.of(new JRadioButton("aa"), new JRadioButton("bb"), new JRadioButton("cc", true)).forEach(b -> {
            p.add(b);
            bg.add(b);
          });
          return p;
        }
      });
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

abstract class AbstractExpansionPanel extends JPanel {
  // OvershadowingSubclassFields: JComponent: private final EventListenerList listenerList = new EventListenerList();
  private ExpansionEvent expansionEvent;
  private final JScrollPane scroll;
  private boolean openFlag;

  protected AbstractExpansionPanel(String title) {
    super(new BorderLayout());
    JButton button = new JButton(title);
    button.addActionListener(e -> {
      setExpanded(!isExpanded());
      fireExpansionEvent();
    });
    scroll = new JScrollPane(makePanel());
    scroll.getVerticalScrollBar().setUnitIncrement(25);
    add(button, BorderLayout.NORTH);
  }

  public abstract Container makePanel();

  public boolean isExpanded() {
    return openFlag;
  }

  public void setExpanded(boolean flg) {
    openFlag = flg;
    if (openFlag) {
      add(scroll);
    } else {
      remove(scroll);
    }
  }

  public void addExpansionListener(ExpansionListener l) {
    listenerList.add(ExpansionListener.class, l);
  }

  public void removeExpansionListener(ExpansionListener l) {
    listenerList.remove(ExpansionListener.class, l);
  }

  // Notify all listeners that have registered interest for
  // notification on this event type.The event instance
  // is lazily created using the parameters passed into
  // the fire method.
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  protected void fireExpansionEvent() {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ExpansionListener.class) {
        // Lazily create the event:
        if (Objects.isNull(expansionEvent)) {
          expansionEvent = new ExpansionEvent(this);
        }
        ((ExpansionListener) listeners[i + 1]).expansionStateChanged(expansionEvent);
      }
    }
  }
}

class ExpansionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  protected ExpansionEvent(Object source) {
    super(source);
  }
}

interface ExpansionListener extends EventListener {
  void expansionStateChanged(ExpansionEvent e);
}
