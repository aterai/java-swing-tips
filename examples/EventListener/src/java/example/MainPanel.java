// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.*;
// import javax.swing.event.EventListenerList;

public final class MainPanel extends JPanel {
  private static final Font FONT12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  private static final Font FONT32 = new Font(Font.SANS_SERIF, Font.PLAIN, 32);

  private MainPanel() {
    super(new BorderLayout());
    MyButton button = new MyButton("JButton");
    addFontChangeListener(button);
    // addFontChangeListener(combo);
    MyLabel label = new MyLabel("JLabel");
    addFontChangeListener(label);

    JMenu menu = new JMenu("Font");
    menu.setToolTipText("Select font size");
    menu.add("32pt").addActionListener(e -> fireFontChangeEvent("font32", FONT32));
    menu.add("12pt").addActionListener(e -> fireFontChangeEvent("font12", FONT12));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    label.setFont(FONT12);
    MyComboBox combo = new MyComboBox();
    combo.setFont(FONT12);
    button.setFont(FONT12);

    JPanel panel = new JPanel();
    panel.add(label);
    panel.add(combo);
    panel.add(button);
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/EventListenerList.html
  // OvershadowingSubclassFields:
  // JComponent: private final EventListenerList listenerList = new EventListenerList();
  // FontChangeEvent fontChangeEvent = null;
  public void addFontChangeListener(FontChangeListener l) {
    listenerList.add(FontChangeListener.class, l);
  }

  public void removeFontChangeListener(FontChangeListener l) {
    listenerList.remove(FontChangeListener.class, l);
  }

  // Notify all listeners that have registered interest in
  // notification on this event type.The event instance
  // is lazily created using the parameters passed into
  // the fire method.
  public void fireFontChangeEvent(String cmd, Font font) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    FontChangeEvent e = new FontChangeEvent(this, cmd, font);
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FontChangeListener.class) {
        // // Lazily create the event:
        // if (fontChangeEvent == null) {
        //   fontChangeEvent = new FontChangeEvent(this);
        // }
        ((FontChangeListener) listeners[i + 1]).fontStateChanged(e);
      }
    }
  }

  // // http://www.asahi-net.or.jp/~dp8t-asm/java/tips/Event.html
  // private final Vector<FontChangeListener> listenerList = new Vector<>();
  // public void addFontChangeListener(FontChangeListener listener) {
  //   if (!listenerList.contains(listener)) { listenerList.add(listener); }
  // }
  // public void removeFontChangeListener(FontChangeListener listener) {
  //   listenerList.remove(listener);
  // }
  // public void fireFontChangeEvent(String cmd, Font font) {
  //   Vector list = (Vector) listenerList.clone();
  //   Enumeration e = list.elements();
  //   FontChangeEvent evt = new FontChangeEvent(this, cmd, font);
  //   while (e.hasMoreElements()) {
  //     FontChangeListener listener = (FontChangeListener) e.nextElement();
  //     listener.fontStateChanged(evt);
  //   }
  //   revalidate();
  // }

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

interface FontChangeListener extends EventListener {
  void fontStateChanged(FontChangeEvent e);
}

class FontChangeEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  private final String command;
  private final Font font;

  protected FontChangeEvent(Object source, String cmd, Font font) {
    super(source);
    this.command = cmd;
    this.font = font;
  }

  public String getCommand() {
    return command;
  }

  public Font getFont() {
    return font;
  }
}

class MyComboBox extends JComboBox<String> implements FontChangeListener {
  protected MyComboBox() {
    super(new DefaultComboBoxModel<>(new String[] {"test test"}));
  }

  @Override public void fontStateChanged(FontChangeEvent e) {
    setFont(e.getFont());
  }
}

class MyLabel extends JLabel implements FontChangeListener {
  protected MyLabel(String str) {
    super(str);
  }

  @Override public void fontStateChanged(FontChangeEvent e) {
    setFont(e.getFont());
  }
}

class MyButton extends JButton implements FontChangeListener {
  protected MyButton(String str) {
    super(str);
  }

  @Override public void fontStateChanged(FontChangeEvent e) {
    setFont(e.getFont());
  }
}
