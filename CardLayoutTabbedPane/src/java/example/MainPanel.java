// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTabbedPane tab1 = new JTabbedPane();
    tab1.setBorder(BorderFactory.createTitledBorder("JTabbedPane"));
    tab1.addTab("1111", new JScrollPane(new JTree()));
    tab1.addTab("2222", new JLabel("JLabel 1"));
    tab1.addTab("3333", new JLabel("JLabel 2"));
    tab1.addTab("4444", new JButton("JButton 1"));

    CardLayoutTabbedPane tab2 = new CardLayoutTabbedPane();
    tab2.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(windows like)"));
    tab2.addTab("default", tab1);
    tab2.addTab("5555", new JScrollPane(new JTree()));
    tab2.addTab("6666", new JLabel("JLabel 3"));
    tab2.addTab("7777", new JLabel("JLabel 4"));
    tab2.addTab("8888", new JButton("JButton 2"));

    UIManager.put("example.TabButton", "TabViewButtonUI");
    UIManager.put("TabViewButtonUI", "example.OperaTabViewButtonUI");
    CardLayoutTabbedPane tab3 = new CardLayoutTabbedPane();
    tab3.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(opera like)"));
    tab3.addTab("9999", new JScrollPane(new JTree()));
    tab3.addTab("000000000", new JLabel("JLabel 5"));
    tab3.addTab("1111", new JLabel("JLabel 6"));
    tab3.addTab("222", new JButton("JButton 3"));

    // add(tab1);
    add(tab2);
    add(tab3);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
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

class CardLayoutTabbedPane extends JPanel {
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel tabPanel = new JPanel(new GridLayout(1, 0, 0, 0));
  private final JPanel contentsPanel = new JPanel(cardLayout);
  private final ButtonGroup bg = new ButtonGroup();

  protected CardLayoutTabbedPane() {
    super(new BorderLayout());
    int left = 1;
    int right = 3;
    tabPanel.setBorder(BorderFactory.createEmptyBorder(1, left, 0, right));
    contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right));

    JPanel tabArea = new JPanel(new BorderLayout());
    tabArea.add(tabPanel);
    tabArea.add(new JLabel("test:"), BorderLayout.WEST);

    add(tabArea, BorderLayout.NORTH);
    add(contentsPanel);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  protected Component createTabComponent(String title, Component comp) {
    TabButton tab = new TabButton(title);
    tab.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        ((AbstractButton) e.getComponent()).setSelected(true);
        cardLayout.show(contentsPanel, title);
      }
    });
    tab.setLayout(new BorderLayout());
    // tab.setLayout(new OverlayLayout(tab));
    JButton close = new JButton(new CloseTabIcon(Color.GRAY)) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(12, 12);
      }
    };
    close.addActionListener(e -> {
      tabPanel.remove(tab);
      contentsPanel.remove(comp);
      boolean oneOrMore = tabPanel.getComponentCount() > 1;
      if (oneOrMore) {
        tabPanel.revalidate();
        TabButton b = (TabButton) tabPanel.getComponent(0);
        b.setSelected(true);
        cardLayout.first(contentsPanel);
      }
      tabPanel.revalidate();
    });
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setFocusPainted(false);
    close.setContentAreaFilled(false);
    close.setPressedIcon(new CloseTabIcon(new Color(0xFE_FE_FE)));
    close.setRolloverIcon(new CloseTabIcon(new Color(0xA0_A0_A0)));

    JPanel p = new JPanel(new BorderLayout());
    p.setOpaque(false);
    p.add(close, BorderLayout.NORTH);
    tab.add(p, BorderLayout.EAST);
    bg.add(tab);
    tab.setSelected(true);
    return tab;
  }

  public void addTab(String title, Component comp) {
    tabPanel.add(createTabComponent(title, comp));
    contentsPanel.add(comp, title);
    cardLayout.show(contentsPanel, title);
  }
}

class TabButton extends JRadioButton {
  private static final String UI_CLASS_ID = "TabViewButtonUI";
  private Color textColor; // = Color.WHITE;
  private Color pressedTc; // = Color.WHITE.darker();
  private Color rolloverTc; // = Color.WHITE;
  private Color rolloverSelTc; // = Color.WHITE;
  private Color selectedTc; // = Color.WHITE;

  protected TabButton() {
    super(null, null);
  }

  protected TabButton(Icon icon) {
    super(null, icon);
  }

  protected TabButton(String text) {
    super(text, null);
  }

  protected TabButton(Action a) {
    super(a);
    // super.setAction(a);
    // updateUI();
  }

  protected TabButton(String text, Icon icon) {
    super(text, icon);
    // updateUI();
  }

  @Override public void updateUI() {
    if (Objects.nonNull(UIManager.get(getUIClassID()))) {
      setUI((TabViewButtonUI) UIManager.getUI(this));
    } else {
      setUI(new BasicTabViewButtonUI());
    }
  }

  @Override public String getUIClassID() {
    return UI_CLASS_ID;
  }

  // @Override public void setUI(TabViewButtonUI ui) {
  //   super.setUI(ui);
  // }

  @Override public TabViewButtonUI getUI() {
    return (TabViewButtonUI) ui;
  }

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isEnabled()) {
      if (model.isPressed() && model.isArmed()) {
        setForeground(getPressedTextColor());
      } else if (model.isSelected()) {
        setForeground(getSelectedTextColor());
      } else if (isRolloverEnabled() && model.isRollover()) {
        setForeground(getRolloverTextColor());
      } else {
        setForeground(getTextColor());
      }
    } else {
      setForeground(Color.GRAY);
    }
    super.fireStateChanged();
  }

  @Override public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    d.width = 16;
    return d;
  }

  @Override public String toString() {
    return "TabButton";
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getPressedTextColor() {
    return pressedTc;
  }

  public Color getRolloverTextColor() {
    return rolloverTc;
  }

  public Color getRolloverSelectedTextColor() {
    return rolloverSelTc;
  }

  public Color getSelectedTextColor() {
    return selectedTc;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setPressedTextColor(Color color) {
    pressedTc = color;
  }

  public void setRolloverTextColor(Color color) {
    rolloverTc = color;
  }

  public void setRolloverSelectedTextColor(Color color) {
    rolloverSelTc = color;
  }

  public void setSelectedTextColor(Color color) {
    selectedTc = color;
  }
}

class CloseTabIcon implements Icon {
  private final Color color;

  protected CloseTabIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(2, 2, 9, 9);
    g2.drawLine(2, 3, 8, 9);
    g2.drawLine(3, 2, 9, 8);
    g2.drawLine(9, 2, 2, 9);
    g2.drawLine(9, 3, 3, 9);
    g2.drawLine(8, 2, 2, 8);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
