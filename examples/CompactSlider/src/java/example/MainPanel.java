// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 10, 10));
    p.setOpaque(false);
    p.add(new CompactSliderPanel1());
    p.add(new CompactSliderPanel2());
    add(p, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class CompactSliderPanel1 extends JPanel {
  /* default */ CompactSliderPanel1() {
    super(new GridLayout(0, 1, 10, 10));
    setOpaque(false);
    add(createCompactSlider1());
    add(createCompactSlider2());
    setBackground(Color.WHITE);
  }

  private static Component createCompactSlider1() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = new FlatProgressBar(m);
    int value = m.getValue();
    int min = m.getMinimum();
    int max = m.getMaximum();
    SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 5);
    JSpinner spinner = new ProgressBarSpinner(model, progressBar);
    initListener(spinner, progressBar);
    return spinner;
  }

  private static Component createCompactSlider2() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = new FlatProgressBar(m);
    int min = m.getMinimum();
    int max = m.getMaximum();
    int value = m.getValue();
    JSpinner spinner = new FlatSpinner(new SpinnerNumberModel(value, min, max, 5));
    initListener(spinner, progressBar);
    return new JLayer<>(spinner, new ProgressBarLayerUI(progressBar));
  }

  private static void initListener(JSpinner spinner, JProgressBar progressBar) {
    spinner.addChangeListener(e -> {
      JSpinner src = (JSpinner) e.getSource();
      progressBar.setValue((Integer) src.getValue());
    });
    spinner.addMouseWheelListener(e -> {
      JSpinner src = (JSpinner) e.getComponent();
      SpinnerNumberModel m = (SpinnerNumberModel) src.getModel();
      int step = e.getWheelRotation() * m.getStepSize().intValue();
      int newValue = (int) src.getValue() - step;
      // Clamp so that min and max can be reached even if the value is not a multiple of step
      int min = (Integer) m.getMinimum();
      int max = (Integer) m.getMaximum();
      src.setValue(Math.min(Math.max(newValue, min), max));
      // Java 21: src.setValue(Math.clamp(newValue, min, max));
    });
  }
}

class FlatProgressBar extends JProgressBar {
  protected FlatProgressBar(BoundedRangeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    setUI(new BasicProgressBarUI());
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());
  }
}

class FlatSpinner extends JSpinner {
  protected FlatSpinner(SpinnerModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    DefaultEditor editor = (DefaultEditor) getEditor();
    editor.setOpaque(false);
    JTextField field = editor.getTextField();
    field.setOpaque(false);
    field.setBorder(BorderFactory.createEmptyBorder());
    UIDefaults d = new UIDefaults();
    Painter<JComponent> painter = (g, c, w, h) -> {
      // empty painter
    };
    String key = "Spinner:Panel:\"Spinner.formattedTextField\"";
    d.put(key + "[Enabled].backgroundPainter", painter);
    d.put(key + "[Focused].backgroundPainter", painter);
    d.put(key + "[Selected].backgroundPainter", painter);
    field.putClientProperty("Nimbus.Overrides", d);
    field.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
  }
}

class ProgressBarLayerUI extends LayerUI<JSpinner> {
  private final JPanel renderer = new JPanel();
  private final JProgressBar progressBar;

  protected ProgressBarLayerUI(JProgressBar progressBar) {
    super();
    this.progressBar = progressBar;
  }

  @Override public void paint(Graphics g, JComponent c) {
    // super.paint(g, c);
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      if (view instanceof JSpinner) {
        JComponent editor = ((JSpinner) view).getEditor();
        Rectangle r = editor.getBounds();
        Graphics2D g2 = (Graphics2D) g.create();
        SwingUtilities.paintComponent(g2, progressBar, renderer, r);
        g2.dispose();
      }
    }
    super.paint(g, c);
  }
}

// The transparent settings of the editor are inherited from FlatSpinner
class ProgressBarSpinner extends FlatSpinner {
  private final JPanel renderer = new JPanel();
  private final JProgressBar progressBar;

  protected ProgressBarSpinner(SpinnerModel model, JProgressBar progressBar) {
    super(model);
    this.progressBar = progressBar;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    JComponent editor = getEditor();
    Rectangle r = editor.getBounds();
    SwingUtilities.paintComponent(g2, progressBar, renderer, r);
    g2.dispose();
  }
}

final class CompactSliderPanel2 extends JPanel {
  /* default */ CompactSliderPanel2() {
    super(new GridLayout(0, 1, 10, 10));
    setOpaque(false);
    add(createCompactSlider3());
    add(createCompactSlider4());
    setBackground(Color.WHITE);
  }

  private static Component createCompactSlider3() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = new FlatProgressBar(m);
    JFormattedTextField field = new ProgressBarFormattedTextField(progressBar);
    field.setValue(m.getValue());
    // Keep the text field in sync when the value is changed by the buttons or the mouse wheel
    m.addChangeListener(e -> field.setValue(m.getValue()));
    // BoundedRangeModel#setValue(...) clamps the value to the range
    field.addMouseWheelListener(e -> m.setValue(m.getValue() - e.getWheelRotation()));
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(field);
    box.add(createButton(-5, m));
    box.add(createButton(+5, m));
    box.add(Box.createHorizontalGlue());
    return box;
  }

  private static JButton createButton(int step, BoundedRangeModel m) {
    String title = String.format("%+d", step);
    JButton button = new JButton(title);
    AutoRepeatHandler handler = new AutoRepeatHandler(step, m);
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
  }

  private static Component createCompactSlider4() {
    JSlider slider = new JSlider(0, 100, 50) {
      @Override public void updateUI() {
        super.updateUI();
        setForeground(Color.LIGHT_GRAY);
        setUI(new FlatSliderUI(this));
        setFocusable(false);
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    JFormattedTextField field = new SliderFormattedTextField(slider);
    field.setValue(slider.getValue());
    slider.addChangeListener(e -> {
      JSlider src = (JSlider) e.getSource();
      field.setValue(src.getValue());
      // BasicSliderUI repaints only the thumb area while dragging,
      // so repaint the whole slider to update the filled track
      src.repaint();
    });
    // JSlider#setValue(...) clamps the value to the range
    slider.addMouseWheelListener(e -> {
      JSlider src = (JSlider) e.getComponent();
      src.setValue(src.getValue() - e.getWheelRotation());
    });
    JPanel p = new OverlayPanel(slider.getPreferredSize());
    p.setLayout(new OverlayLayout(p));
    p.setOpaque(false);
    p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    p.add(field);
    p.add(slider);
    Box box = Box.createHorizontalBox();
    box.add(p);
    box.add(Box.createHorizontalStrut(2));
    box.add(createButton(-5, slider.getModel()));
    box.add(createButton(+5, slider.getModel()));
    box.add(Box.createHorizontalGlue());
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(p);
    panel.add(box, BorderLayout.EAST);
    return panel;
  }
}

class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final int step;
  private final BoundedRangeModel model;
  private JButton arrowButton;

  protected AutoRepeatHandler(int step, BoundedRangeModel model) {
    super();
    this.step = step;
    this.model = model;
    autoRepeatTimer = new Timer(60, this);
    autoRepeatTimer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o instanceof Timer) {
      boolean released = Objects.nonNull(arrowButton) && !arrowButton.getModel().isPressed();
      if (released && autoRepeatTimer.isRunning()) {
        autoRepeatTimer.stop();
      }
    } else if (o instanceof JButton) {
      arrowButton = (JButton) o;
    }
    // The text field is updated by the ChangeListener of the model,
    // and BoundedRangeModel#setValue(...) clamps the value to the range
    model.setValue(model.getValue() + step);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
      autoRepeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    autoRepeatTimer.stop();
  }

  @Override public void mouseExited(MouseEvent e) {
    if (autoRepeatTimer.isRunning()) {
      autoRepeatTimer.stop();
    }
  }
}

class NumberFormatterFactory extends DefaultFormatterFactory {
  protected NumberFormatterFactory() {
    // An AbstractFormatter is installed into a single JFormattedTextField,
    // so create a new instance for each factory instead of sharing a static one
    super(createFormatter());
  }

  private static NumberFormatter createFormatter() {
    NumberFormatter formatter = new NumberFormatter();
    formatter.setValueClass(Integer.class);
    ((NumberFormat) formatter.getFormat()).setGroupingUsed(false);
    return formatter;
  }
}

class FlatSliderUI extends BasicSliderUI {
  protected FlatSliderUI(JSlider slider) {
    super(slider);
  }

  @Override public void paintThumb(Graphics g) {
    // super.paintThumb(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(slider.getForeground());
    Rectangle r = SwingUtilities.calculateInnerArea(slider, null);
    g2.fillRect(thumbRect.x, r.y, thumbRect.width, r.height);
    g2.dispose();
  }

  @Override public void paintTrack(Graphics g) {
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      Graphics2D g2 = (Graphics2D) g.create();
      int middleOfThumb = thumbRect.x + thumbRect.width / 2;
      Rectangle r = SwingUtilities.calculateInnerArea(slider, null);
      if (drawInverted()) {
        // Fill from the thumb to the right edge
        r.width = r.x + r.width - middleOfThumb;
        r.x = middleOfThumb;
      } else {
        r.width = middleOfThumb - r.x;
      }
      g2.setPaint(slider.getForeground());
      g2.fill(r);
      g2.dispose();
    } else {
      super.paintTrack(g);
    }
  }

  @Override public void paintFocus(Graphics g) {
    // super.paintFocus(g);
  }
}

// class MouseEventHandler extends MouseAdapter {
//   private final Component target;
//
//   protected MouseEventHandler(Component target) {
//     super();
//     this.target = target;
//   }
//
//   @Override public void mouseClicked(MouseEvent e) {
//     super.mouseClicked(e);
//     dispatchEvent(e);
//   }
//
//   @Override public void mousePressed(MouseEvent e) {
//     super.mousePressed(e);
//     Component c = e.getComponent();
//     EventQueue.invokeLater(() -> {
//       if (!c.hasFocus()) {
//         dispatchEvent(e);
//       }
//     });
//   }
//
//   @Override public void mouseReleased(MouseEvent e) {
//     super.mouseReleased(e);
//     dispatchEvent(e);
//   }
//
//   @Override public void mouseDragged(MouseEvent e) {
//     super.mouseDragged(e);
//     dispatchEvent(e);
//   }
//
//   @Override public void mouseWheelMoved(MouseWheelEvent e) {
//     super.mouseWheelMoved(e);
//     dispatchEvent(e);
//   }
//
//   private void dispatchEvent(MouseEvent e) {
//     Component c = e.getComponent();
//     MouseEvent ev = SwingUtilities.convertMouseEvent(c, e, target);
//     target.dispatchEvent(ev);
//     target.repaint();
//   }
// }

class ProgressBarFormattedTextField extends JFormattedTextField {
  private final JPanel renderer = new JPanel();
  private final JProgressBar progressBar;

  protected ProgressBarFormattedTextField(JProgressBar progressBar) {
    super();
    this.progressBar = progressBar;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setFormatterFactory(new NumberFormatterFactory());
    setHorizontalAlignment(RIGHT);
    setColumns(16);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    SwingUtilities.paintComponent(g2, progressBar, renderer, r);
    g2.dispose();
    super.paintComponent(g);
  }

  @Override public void commitEdit() throws ParseException {
    super.commitEdit();
    Optional.ofNullable(getValue())
        .filter(Integer.class::isInstance)
        .map(Integer.class::cast)
        .ifPresent(progressBar::setValue);
  }
}

class SliderFormattedTextField extends JFormattedTextField {
  private final JSlider slider;

  protected SliderFormattedTextField(JSlider slider) {
    super();
    this.slider = slider;
  }

  @Override public void updateUI() {
    // removeMouseListener(handler);
    // removeMouseMotionListener(handler);
    super.updateUI();
    setFormatterFactory(new NumberFormatterFactory());
    setHorizontalAlignment(RIGHT);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setColumns(3);
    setAlignmentX(RIGHT_ALIGNMENT);
    // handler = new MouseEventHandler(slider);
    // addMouseListener(handler);
    // addMouseMotionListener(handler);
  }

  @Override public void commitEdit() throws ParseException {
    super.commitEdit();
    Optional.ofNullable(getValue())
        .filter(Integer.class::isInstance)
        .map(Integer.class::cast)
        .ifPresent(slider::setValue);
  }

  @Override public Dimension getMaximumSize() {
    return super.getPreferredSize();
  }
}

class OverlayPanel extends JPanel {
  private final Dimension size;

  protected OverlayPanel(Dimension size) {
    super();
    this.size = size;
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }

  @Override public Dimension getPreferredSize() {
    return size;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
