// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        return new Dimension(32, 32);
      }
    };
    label.setOpaque(true);
    label.setBackground(Color.WHITE);

    RecentSwatchPanel switchPanel = new RecentSwatchPanel();
    switchPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    switchPanel.colors[0] = Color.RED;
    switchPanel.colors[1] = Color.GREEN;
    switchPanel.colors[2] = Color.BLUE;

    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> showColorChooser(switchPanel, label));

    add(switchPanel);
    add(label);
    add(button);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private void showColorChooser(RecentSwatchPanel switchPanel, JLabel label) {
    JColorChooser cc = new JColorChooser();
    List<AbstractColorChooserPanel> choosers = getColorChooserPanels(cc, switchPanel);
    cc.setChooserPanels(choosers.toArray(new AbstractColorChooserPanel[0]));
    ColorTracker ok = new ColorTracker(cc);
    Component parent = getRootPane();
    String title = "JColorChooser";
    JDialog dialog = JColorChooser.createDialog(parent, title, true, cc, ok, null);
    dialog.addComponentListener(new ComponentAdapter() {
      @Override public void componentHidden(ComponentEvent e) {
        ((Window) e.getComponent()).dispose();
      }
    });
    dialog.setVisible(true); // blocks until user brings dialog down...
    switchPanel.repaint();
    Color color = ok.getColor();
    if (color != null) {
      label.setBackground(color);
    }
  }

  private static List<AbstractColorChooserPanel> getColorChooserPanels(
      JColorChooser cc, RecentSwatchPanel switchPanel) {
    AbstractColorChooserPanel[] panels = cc.getChooserPanels();
    // https://stackoverflow.com/questions/10793916/jcolorchooser-save-restore-recent-colors-in-swatches-panel
    List<AbstractColorChooserPanel> choosers = new ArrayList<>(Arrays.asList(panels));
    choosers.remove(0);
    MySwatchChooserPanel swatch = new MySwatchChooserPanel();
    swatch.addPropertyChangeListener("ancestor", event -> {
      Color[] colors = swatch.recentSwatchPanel.colors;
      if (event.getNewValue() == null) {
        System.arraycopy(colors, 0, switchPanel.colors, 0, colors.length);
      } else {
        System.arraycopy(switchPanel.colors, 0, colors, 0, colors.length);
      }
    });
    choosers.add(0, swatch);
    return choosers;
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser c) {
    chooser = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}

// copied from javax/swing/colorchooser/DefaultSwatchChooserPanel.java
@SuppressWarnings("PMD.LongVariable")
class MySwatchChooserPanel extends AbstractColorChooserPanel {
  protected RecentSwatchPanel recentSwatchPanel;
  private SwatchPanel swatchPanel;
  private transient MouseListener mainSwatchListener;
  private transient MouseListener recentSwatchListener;
  private transient KeyListener mainSwatchKeyListener;
  private transient KeyListener recentSwatchKeyListener;

  protected MySwatchChooserPanel() {
    super();
    setInheritsPopupMenu(true);
  }

  @Override public final void setInheritsPopupMenu(boolean value) {
    super.setInheritsPopupMenu(value);
  }

  @Override public String getDisplayName() {
    return UIManager.getString("ColorChooser.swatchesNameText", getLocale());
  }

  @Override public Icon getSmallDisplayIcon() {
    return null;
  }

  @Override public Icon getLargeDisplayIcon() {
    return null;
  }

  @Override protected void buildChooser() {
    swatchPanel = new MainSwatchPanel();
    swatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, getDisplayName());
    swatchPanel.setInheritsPopupMenu(true);

    String recentStr = UIManager.getString("ColorChooser.swatchesRecentText", getLocale());
    recentSwatchPanel = new RecentSwatchPanel();
    recentSwatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, recentStr);

    mainSwatchKeyListener = new MainSwatchKeyListener();
    mainSwatchListener = new MainSwatchListener();
    swatchPanel.addMouseListener(mainSwatchListener);
    swatchPanel.addKeyListener(mainSwatchKeyListener);
    recentSwatchListener = new RecentSwatchListener();
    recentSwatchKeyListener = new RecentSwatchKeyListener();
    recentSwatchPanel.addMouseListener(recentSwatchListener);
    recentSwatchPanel.addKeyListener(recentSwatchKeyListener);

    JPanel mainHolder = new JPanel(new BorderLayout());
    Border outside = BorderFactory.createLineBorder(Color.BLACK);
    Border inside = BorderFactory.createLineBorder(Color.WHITE);
    Border border = BorderFactory.createCompoundBorder(outside, inside);
    mainHolder.setBorder(border);
    mainHolder.add(swatchPanel, BorderLayout.CENTER);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LAST_LINE_START;
    gbc.gridwidth = 1;
    gbc.gridheight = 2;
    Insets oldInsets = gbc.insets;
    gbc.insets = new Insets(0, 0, 0, 10);

    JPanel superHolder = new JPanel(new GridBagLayout());
    superHolder.add(mainHolder, gbc);
    gbc.insets = oldInsets;

    recentSwatchPanel.setInheritsPopupMenu(true);
    JPanel recentHolder = new JPanel(new BorderLayout());
    recentHolder.setBorder(border);
    recentHolder.setInheritsPopupMenu(true);
    recentHolder.add(recentSwatchPanel, BorderLayout.CENTER);

    JLabel l = new JLabel(recentStr);
    l.setLabelFor(recentSwatchPanel);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.weighty = 1.0;
    superHolder.add(l, gbc);

    gbc.weighty = 0;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.insets = new Insets(0, 0, 0, 2);
    superHolder.add(recentHolder, gbc);
    superHolder.setInheritsPopupMenu(true);

    add(superHolder);
  }

  @SuppressWarnings("PMD.NullAssignment")
  @Override public void uninstallChooserPanel(JColorChooser enclosingChooser) {
    super.uninstallChooserPanel(enclosingChooser);
    swatchPanel.removeMouseListener(mainSwatchListener);
    swatchPanel.removeKeyListener(mainSwatchKeyListener);
    recentSwatchPanel.removeMouseListener(recentSwatchListener);
    recentSwatchPanel.removeKeyListener(recentSwatchKeyListener);
    swatchPanel = null;
    recentSwatchPanel = null;
    mainSwatchListener = null;
    mainSwatchKeyListener = null;
    recentSwatchListener = null;
    recentSwatchKeyListener = null;
    removeAll();
  }

  @Override public void updateChooser() {
    // empty
  }

  public void setSelectedColor2(Color color) {
    ColorSelectionModel model = getColorSelectionModel();
    if (model != null) {
      model.setSelectedColor(color);
    }
  }

  private final class RecentSwatchKeyListener extends KeyAdapter {
    @Override public void keyPressed(KeyEvent e) {
      if (KeyEvent.VK_SPACE == e.getKeyCode()) {
        Color color = recentSwatchPanel.getSelectedColor();
        setSelectedColor2(color);
      }
    }
  }

  private final class MainSwatchKeyListener extends KeyAdapter {
    @Override public void keyPressed(KeyEvent e) {
      if (KeyEvent.VK_SPACE == e.getKeyCode()) {
        Color color = swatchPanel.getSelectedColor();
        setSelectedColor2(color);
        recentSwatchPanel.setMostRecentColor(color);
      }
    }
  }

  private final class RecentSwatchListener extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      if (isEnabled()) {
        Color color = recentSwatchPanel.getColorForLocation(e.getX(), e.getY());
        recentSwatchPanel.setSelectedColorFromLocation(e.getX(), e.getY());
        setSelectedColor2(color);
        recentSwatchPanel.requestFocusInWindow();
      }
    }
  }

  private final class MainSwatchListener extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      if (isEnabled()) {
        Color color = swatchPanel.getColorForLocation(e.getX(), e.getY());
        setSelectedColor2(color);
        swatchPanel.setSelectedColorFromLocation(e.getX(), e.getY());
        recentSwatchPanel.setMostRecentColor(color);
        swatchPanel.requestFocusInWindow();
      }
    }
  }
}

class SwatchPanel extends JPanel {
  protected Color[] colors;
  protected Dimension swatchSize;
  protected Dimension numSwatches;
  protected Dimension gap;
  private int selRow;
  private int selCol;
  private transient Handler handler;

  protected SwatchPanel() {
    super();
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override public void updateUI() {
    removeFocusListener(handler);
    removeKeyListener(handler);
    super.updateUI();
    initValues();
    initColors();
    // setToolTipText(""); // register for events
    setOpaque(true);
    setBackground(Color.WHITE);
    setFocusable(true);
    setInheritsPopupMenu(true);

    handler = new Handler();
    addFocusListener(handler);
    addKeyListener(handler);
  }

  private final class Handler extends KeyAdapter implements FocusListener {
    @SuppressWarnings({
        "PMD.CyclomaticComplexity",
        "PMD.CognitiveComplexity",
        "CyclomaticComplexity"
    })
    @Override public void keyPressed(KeyEvent e) {
      boolean isLeftToRight = SwatchPanel.this.getComponentOrientation().isLeftToRight();
      switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          if (selRow > 0) {
            selRow--;
            repaint();
          }
          break;
        case KeyEvent.VK_DOWN:
          if (selRow < numSwatches.height - 1) {
            selRow++;
            repaint();
          }
          break;
        case KeyEvent.VK_LEFT:
          if (selCol > 0 && isLeftToRight) {
            selCol--;
            repaint();
          } else if (selCol < numSwatches.width - 1 && !isLeftToRight) {
            selCol++;
            repaint();
          }
          break;
        case KeyEvent.VK_RIGHT:
          if (selCol < numSwatches.width - 1 && isLeftToRight) {
            selCol++;
            repaint();
          } else if (selCol > 0 && !isLeftToRight) {
            selCol--;
            repaint();
          }
          break;
        case KeyEvent.VK_HOME:
          selCol = 0;
          selRow = 0;
          repaint();
          break;
        case KeyEvent.VK_END:
          selCol = numSwatches.width - 1;
          selRow = numSwatches.height - 1;
          repaint();
          break;
        default:
          break;
      }
    }

    @Override public void focusGained(FocusEvent e) {
      repaint();
    }

    @Override public void focusLost(FocusEvent e) {
      repaint();
    }
  }

  public Color getSelectedColor() {
    return getColorForCell(selCol, selRow);
  }

  protected void initValues() {
    // empty
  }

  @Override public void paintComponent(Graphics g) {
    Color defColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    boolean ltr = getComponentOrientation().isLeftToRight();
    int sw = swatchSize.width;
    int sh = swatchSize.height;
    int gw = gap.width;
    int gh = gap.height;
    for (int row = 0; row < numSwatches.height; row++) {
      int y = row * (sh + gh);
      for (int col = 0; col < numSwatches.width; col++) {
        Color c = Optional.ofNullable(getColorForCell(col, row)).orElse(defColor);
        g.setColor(c);
        int x = ltr ? col * (sw + gw) : (numSwatches.width - col - 1) * (sw + gw);
        g.fillRect(x, y, sw, sh);
        g.setColor(Color.BLACK);
        g.drawLine(x + sw - 1, y, x + sw - 1, y + sh - 1);
        g.drawLine(x, y + sh - 1, x + sw - 1, y + sh - 1);
        if (selRow == row && selCol == col && isFocusOwner()) {
          Color c2 = getFocusColor(c);
          g.setColor(c2);
          g.drawLine(x, y, x + sw - 1, y);
          g.drawLine(x, y, x, y + sh - 1);
          g.drawLine(x + sw - 1, y, x + sw - 1, y + sh - 1);
          g.drawLine(x, y + sh - 1, x + sw - 1, y + sh - 1);
          g.drawLine(x, y, x + sw - 1, y + sh - 1);
          g.drawLine(x, y + sh - 1, x + sw - 1, y);
        }
      }
    }
  }

  private static Color getFocusColor(Color c) {
    int r = c.getRed() < 125 ? 255 : 0;
    int g = c.getGreen() < 125 ? 255 : 0;
    int b = c.getBlue() < 125 ? 255 : 0;
    return new Color(r, g, b);
  }

  @Override public Dimension getPreferredSize() {
    int x = numSwatches.width * (swatchSize.width + gap.width) - 1;
    int y = numSwatches.height * (swatchSize.height + gap.height) - 1;
    return new Dimension(x, y);
  }

  protected void initColors() {
    // empty
  }

  @Override public String getToolTipText(MouseEvent e) {
    return Optional.ofNullable(getColorForLocation(e.getX(), e.getY()))
        .map(c -> c.getRed() + ", " + c.getGreen() + ", " + c.getBlue())
        .orElse(null);
  }

  public void setSelectedColorFromLocation(int x, int y) {
    if (getComponentOrientation().isLeftToRight()) {
      selCol = x / (swatchSize.width + gap.width);
    } else {
      selCol = numSwatches.width - x / (swatchSize.width + gap.width) - 1;
    }
    selRow = y / (swatchSize.height + gap.height);
    repaint();
  }

  public Color getColorForLocation(int x, int y) {
    boolean leftToRight = getComponentOrientation().isLeftToRight();
    int w = swatchSize.width + gap.width;
    int column = leftToRight ? x / w : numSwatches.width - x / w - 1;
    int h = swatchSize.height + gap.height;
    int row = y / h;
    return getColorForCell(column, row);
  }

  private Color getColorForCell(int column, int row) {
    return colors[(row * numSwatches.width) + column];
  }
}

class RecentSwatchPanel extends SwatchPanel {
  @Override protected void initValues() {
    swatchSize = UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize");
    numSwatches = new Dimension(5, 7);
    gap = new Dimension(1, 1);
  }

  @Override protected void initColors() {
    Color defaultRecent = null;
    // = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
    int numColors = numSwatches.width * numSwatches.height;
    colors = new Color[numColors];
    for (int i = 0; i < numColors; i++) {
      colors[i] = defaultRecent;
    }
  }

  public void setMostRecentColor(Color c) {
    System.arraycopy(colors, 0, colors, 1, colors.length - 1);
    colors[0] = c;
    repaint();
  }
}

class MainSwatchPanel extends SwatchPanel {
  @Override protected void initValues() {
    swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize", getLocale());
    numSwatches = new Dimension(31, 9);
    gap = new Dimension(1, 1);
  }

  @Override protected void initColors() {
    int[] rawValues = initRawValues();
    int numColors = rawValues.length / 3;
    colors = new Color[numColors];
    for (int i = 0; i < numColors; i++) {
      int v = i * 3;
      colors[i] = new Color(rawValues[v], rawValues[v + 1], rawValues[v + 2]);
    }
  }

  @SuppressWarnings("MethodLength")
  private int[] initRawValues() {
    return new int[] {
        255, 255, 255, // first row.
        204, 255, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        204, 204, 255,
        255, 204, 255,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 204, 204,
        255, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 255, 204,
        204, 204, 204,  // second row.
        153, 255, 255,
        153, 204, 255,
        153, 153, 255,
        153, 153, 255,
        153, 153, 255,
        153, 153, 255,
        153, 153, 255,
        153, 153, 255,
        153, 153, 255,
        204, 153, 255,
        255, 153, 255,
        255, 153, 204,
        255, 153, 153,
        255, 153, 153,
        255, 153, 153,
        255, 153, 153,
        255, 153, 153,
        255, 153, 153,
        255, 153, 153,
        255, 204, 153,
        255, 255, 153,
        204, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 153,
        153, 255, 204,
        204, 204, 204,  // third row
        102, 255, 255,
        102, 204, 255,
        102, 153, 255,
        102, 102, 255,
        102, 102, 255,
        102, 102, 255,
        102, 102, 255,
        102, 102, 255,
        153, 102, 255,
        204, 102, 255,
        255, 102, 255,
        255, 102, 204,
        255, 102, 153,
        255, 102, 102,
        255, 102, 102,
        255, 102, 102,
        255, 102, 102,
        255, 102, 102,
        255, 153, 102,
        255, 204, 102,
        255, 255, 102,
        204, 255, 102,
        153, 255, 102,
        102, 255, 102,
        102, 255, 102,
        102, 255, 102,
        102, 255, 102,
        102, 255, 102,
        102, 255, 153,
        102, 255, 204,
        153, 153, 153, // fourth row
        51, 255, 255,
        51, 204, 255,
        51, 153, 255,
        51, 102, 255,
        51, 51, 255,
        51, 51, 255,
        51, 51, 255,
        102, 51, 255,
        153, 51, 255,
        204, 51, 255,
        255, 51, 255,
        255, 51, 204,
        255, 51, 153,
        255, 51, 102,
        255, 51, 51,
        255, 51, 51,
        255, 51, 51,
        255, 102, 51,
        255, 153, 51,
        255, 204, 51,
        255, 255, 51,
        204, 255, 51,
        153, 255, 51,
        102, 255, 51,
        51, 255, 51,
        51, 255, 51,
        51, 255, 51,
        51, 255, 102,
        51, 255, 153,
        51, 255, 204,
        153, 153, 153, // Fifth row
        0, 255, 255,
        0, 204, 255,
        0, 153, 255,
        0, 102, 255,
        0, 51, 255,
        0, 0, 255,
        51, 0, 255,
        102, 0, 255,
        153, 0, 255,
        204, 0, 255,
        255, 0, 255,
        255, 0, 204,
        255, 0, 153,
        255, 0, 102,
        255, 0, 51,
        255, 0, 0,
        255, 51, 0,
        255, 102, 0,
        255, 153, 0,
        255, 204, 0,
        255, 255, 0,
        204, 255, 0,
        153, 255, 0,
        102, 255, 0,
        51, 255, 0,
        0, 255, 0,
        0, 255, 51,
        0, 255, 102,
        0, 255, 153,
        0, 255, 204,
        102, 102, 102, // sixth row
        0, 204, 204,
        0, 204, 204,
        0, 153, 204,
        0, 102, 204,
        0, 51, 204,
        0, 0, 204,
        51, 0, 204,
        102, 0, 204,
        153, 0, 204,
        204, 0, 204,
        204, 0, 204,
        204, 0, 204,
        204, 0, 153,
        204, 0, 102,
        204, 0, 51,
        204, 0, 0,
        204, 51, 0,
        204, 102, 0,
        204, 153, 0,
        204, 204, 0,
        204, 204, 0,
        204, 204, 0,
        153, 204, 0,
        102, 204, 0,
        51, 204, 0,
        0, 204, 0,
        0, 204, 51,
        0, 204, 102,
        0, 204, 153,
        0, 204, 204,
        102, 102, 102, // seventh row
        0, 153, 153,
        0, 153, 153,
        0, 153, 153,
        0, 102, 153,
        0, 51, 153,
        0, 0, 153,
        51, 0, 153,
        102, 0, 153,
        153, 0, 153,
        153, 0, 153,
        153, 0, 153,
        153, 0, 153,
        153, 0, 153,
        153, 0, 102,
        153, 0, 51,
        153, 0, 0,
        153, 51, 0,
        153, 102, 0,
        153, 153, 0,
        153, 153, 0,
        153, 153, 0,
        153, 153, 0,
        153, 153, 0,
        102, 153, 0,
        51, 153, 0,
        0, 153, 0,
        0, 153, 51,
        0, 153, 102,
        0, 153, 153,
        0, 153, 153,
        51, 51, 51, // eighth row
        0, 102, 102,
        0, 102, 102,
        0, 102, 102,
        0, 102, 102,
        0, 51, 102,
        0, 0, 102,
        51, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 102,
        102, 0, 51,
        102, 0, 0,
        102, 51, 0,
        102, 102, 0,
        102, 102, 0,
        102, 102, 0,
        102, 102, 0,
        102, 102, 0,
        102, 102, 0,
        102, 102, 0,
        51, 102, 0,
        0, 102, 0,
        0, 102, 51,
        0, 102, 102,
        0, 102, 102,
        0, 102, 102,
        0, 0, 0, // ninth row
        0, 51, 51,
        0, 51, 51,
        0, 51, 51,
        0, 51, 51,
        0, 51, 51,
        0, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 51,
        51, 0, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        51, 51, 0,
        0, 51, 0,
        0, 51, 51,
        0, 51, 51,
        0, 51, 51,
        0, 51, 51,
        51, 51, 51
    };
  }
}
