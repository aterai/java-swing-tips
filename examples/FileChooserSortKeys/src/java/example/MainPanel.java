// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public final class MainPanel extends JPanel {
  // private transient List<? extends RowSorter.SortKey> sortKeys = null;
  private final SpinnerNumberModel model = new SpinnerNumberModel(0, -1, 3, 1);
  private final JComboBox<SortOrder> combo = new JComboBox<>(SortOrder.values());

  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JFileChooser fileChooser = makeFileChooser();
    JButton button = new JButton("open") {
      @Override public void updateUI() {
        super.updateUI();
        SwingUtilities.updateComponentTreeUI(fileChooser);
      }
    };
    button.addActionListener(e -> {
      int retValue = fileChooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(new JLabel("SortKey column:"));
    p.add(new JSpinner(model));
    p.add(combo);
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private JFileChooser makeFileChooser() {
    return new JFileChooser() {
      private transient AncestorListener handler;

      @Override public void updateUI() {
        removeAncestorListener(handler);
        super.updateUI();
        handler = new AncestorListener() {
          @Override public void ancestorAdded(AncestorEvent e) {
            JFileChooser fc = (JFileChooser) e.getComponent();
            SwingUtils.setViewTypeDetails(fc);
            SwingUtils.descendants(fc)
                .filter(JTable.class::isInstance)
                .map(JTable.class::cast)
                .findFirst()
                .ifPresent(table -> {
                  List<?> sortKeys = table.getRowSorter().getSortKeys();
                  int col = model.getNumber().intValue();
                  if (col < 0) {
                    table.getRowSorter().setSortKeys(Collections.emptyList());
                  } else if (sortKeys.isEmpty() && col < table.getColumnCount()) {
                    SortOrder order = combo.getItemAt(combo.getSelectedIndex());
                    RowSorter.SortKey key = new RowSorter.SortKey(col, order);
                    table.getRowSorter().setSortKeys(Collections.singletonList(key));
                  }
                });
          }

          @Override public void ancestorRemoved(AncestorEvent e) {
            // SwingUtils.descendants(e.getComponent())
            //     .filter(JTable.class::isInstance)
            //     .map(JTable.class::cast)
            //     .findFirst()
            //     .ifPresent(table -> sortKeys = table.getRowSorter().getSortKeys());
          }

          @Override public void ancestorMoved(AncestorEvent e) {
            // not need
          }
        };
        addAncestorListener(handler);
      }
    };
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

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static void setViewTypeDetails(JFileChooser fc) {
    String cmd = "viewTypeDetails";
    Action act = fc.getActionMap().get(cmd);
    if (Objects.nonNull(act)) {
      act.actionPerformed(new ActionEvent(fc, ActionEvent.ACTION_PERFORMED, cmd));
    }
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }
}
