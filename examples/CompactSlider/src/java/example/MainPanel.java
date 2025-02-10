// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
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
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

@SuppressWarnings("PMD.TooManyMethods")
public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 15, 15));
    p.setOpaque(false);
    p.add(makeCompactSlider1());
    p.add(makeCompactSlider2());
    p.add(makeCompactSlider3());
    p.add(makeCompactSlider4());
    add(p, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeCompactSlider1() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = makeProgressBar(m);
    JSpinner spinner = makeSpinner(progressBar);
    initListener(spinner, progressBar);
    return spinner;
  }

  private static JProgressBar makeProgressBar(BoundedRangeModel m) {
    return new JProgressBar(m) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicProgressBarUI());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
      }
    };
  }

  private static JSpinner makeSpinner(JProgressBar progressBar) {
    BoundedRangeModel m = progressBar.getModel();
    int value = m.getValue();
    int min = m.getMinimum();
    int max = m.getMaximum();
    return new JSpinner(new SpinnerNumberModel(value, min, max, 5)) {
      private final JPanel renderer = new JPanel();

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
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
        field.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
      }

      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        JComponent editor = getEditor();
        Rectangle r = editor.getBounds();
        SwingUtilities.paintComponent(g2, progressBar, renderer, r);
        g2.dispose();
      }
    };
  }

  private static Component makeCompactSlider2() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = makeProgressBar(m);
    JSpinner spinner = makeSpinner2(m);
    initListener(spinner, progressBar);
    LayerUI<JSpinner> layerUI = new LayerUI<JSpinner>() {
      private final JPanel renderer = new JPanel();

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
    };
    return new JLayer<>(spinner, layerUI);
  }

  private static JSpinner makeSpinner2(BoundedRangeModel m) {
    int min = m.getMinimum();
    int max = m.getMaximum();
    int value = m.getValue();
    return new JSpinner(new SpinnerNumberModel(value, min, max, 5)) {
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
        field.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
      }
    };
  }

  private static void initListener(JSpinner spinner, JProgressBar progressBar) {
    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      progressBar.setValue((Integer) source.getValue());
    });
    spinner.addMouseWheelListener(e -> {
      JSpinner source = (JSpinner) e.getComponent();
      SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
      Integer oldValue = (Integer) source.getValue();
      Integer intValue = oldValue - e.getWheelRotation() * model.getStepSize().intValue();
      Integer max = (Integer) model.getMaximum();
      Integer min = (Integer) model.getMinimum();
      if (min <= intValue && intValue <= max) {
        source.setValue(intValue);
      }
    });
  }

  private static Component makeCompactSlider3() {
    BoundedRangeModel m = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JProgressBar progressBar = makeProgressBar(m);
    JTextField field = makeTextField(progressBar);
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(field);
    box.add(makeButton(-5, field, progressBar.getModel()));
    box.add(makeButton(+5, field, progressBar.getModel()));
    box.add(Box.createHorizontalGlue());
    return box;
  }

  private static JTextField makeTextField(JProgressBar progressBar) {
    JFormattedTextField field = new JFormattedTextField() {
      private final JPanel renderer = new JPanel();

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setFormatterFactory(new NumberFormatterFactory());
        setHorizontalAlignment(RIGHT);
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
    };
    field.setHorizontalAlignment(SwingConstants.RIGHT);
    field.setOpaque(false);
    field.setColumns(16);
    field.setValue(50);
    field.addMouseWheelListener(e -> {
      JFormattedTextField source = (JFormattedTextField) e.getComponent();
      BoundedRangeModel model = progressBar.getModel();
      Integer oldValue = (Integer) source.getValue();
      int intValue = oldValue - e.getWheelRotation();
      int max = model.getMaximum();
      int min = model.getMinimum();
      if (min <= intValue && intValue <= max) {
        source.setValue(intValue);
        progressBar.setValue(intValue);
      }
    });
    return field;
  }

  private static JButton makeButton(int step, JTextComponent view, BoundedRangeModel m) {
    String title = String.format("%+d", step);
    JButton button = new JButton(title);
    AutoRepeatHandler handler = new AutoRepeatHandler(step, view, m);
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
  }

  private static Component makeCompactSlider4() {
    JSlider slider = new JSlider(0, 100, 50) {
      @Override public void updateUI() {
        super.updateUI();
        setForeground(Color.LIGHT_GRAY);
        setUI(new FlatSliderUI(this));
        setFocusable(false);
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    JFormattedTextField field = new JFormattedTextField() {
      @Override public void updateUI() {
        // removeMouseListener(handler);
        // removeMouseMotionListener(handler);
        super.updateUI();
        setFormatterFactory(new NumberFormatterFactory());
        setHorizontalAlignment(RIGHT);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
    };
    field.setColumns(3);
    field.setValue(slider.getValue());
    field.setHorizontalAlignment(SwingConstants.RIGHT);
    field.setAlignmentX(RIGHT_ALIGNMENT);
    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      field.setValue(source.getValue());
      source.repaint();
    });
    slider.addMouseWheelListener(e -> {
      JSlider source = (JSlider) e.getComponent();
      int oldValue = source.getValue();
      int intValue = oldValue - e.getWheelRotation();
      int max = source.getMaximum();
      int min = source.getMinimum();
      if (min <= intValue && intValue <= max) {
        source.setValue(intValue);
        field.setValue(intValue);
      }
    });
    JPanel p = makeOverlayPanel(slider.getPreferredSize());
    p.add(field);
    p.add(slider);
    Box box = Box.createHorizontalBox();
    box.add(p);
    box.add(Box.createHorizontalStrut(2));
    box.add(makeButton(-5, field, slider.getModel()));
    box.add(makeButton(+5, field, slider.getModel()));
    box.add(Box.createHorizontalGlue());
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(p);
    panel.add(box, BorderLayout.EAST);
    return panel;
  }

  private static JPanel makeOverlayPanel(Dimension size) {
    JPanel p = new JPanel() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }

      @Override public Dimension getPreferredSize() {
        return size;
      }
    };
    p.setLayout(new OverlayLayout(p));
    p.setOpaque(false);
    p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (UnsupportedLookAndFeelException ignored) {
    //     Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //     ex.printStackTrace();
    //     return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final BigInteger extent;
  private final JTextComponent view;
  private final BoundedRangeModel model;
  private JButton arrowButton;

  protected AutoRepeatHandler(int step, JTextComponent view, BoundedRangeModel model) {
    super();
    this.extent = BigInteger.valueOf(step);
    this.view = view;
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
    BigInteger i = new BigInteger(view.getText());
    BigInteger iv = i.add(extent);
    model.setValue(iv.intValue());
    view.setText(String.valueOf(model.getValue()));
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
  private static final NumberFormatter FORMATTER = new NumberFormatter();

  static {
    FORMATTER.setValueClass(Integer.class);
    ((NumberFormat) FORMATTER.getFormat()).setGroupingUsed(false);
  }

  protected NumberFormatterFactory() {
    super(FORMATTER, FORMATTER, FORMATTER);
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
      int fillWidth;
      if (drawInverted()) {
        int trackRight = trackRect.width - 1;
        int fillRight = slider.isEnabled() ? trackRight - 2 : trackRight - 1;
        fillWidth = fillRight - middleOfThumb;
      } else {
        int trackLeft = 0;
        int fillLeft = slider.isEnabled() ? trackLeft + 1 : trackLeft;
        fillWidth = middleOfThumb - fillLeft;
      }
      g2.setPaint(slider.getForeground());
      Rectangle r = SwingUtilities.calculateInnerArea(slider, null);
      r.width = fillWidth;
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

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
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
