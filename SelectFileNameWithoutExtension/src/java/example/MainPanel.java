// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("ListView");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      descendants(chooser)
          .filter(JList.class::isInstance)
          .map(JList.class::cast)
          .findFirst()
          .ifPresent(MainPanel::addCellEditorListener);
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button3 = new JButton("DetailsView");
    button3.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      String cmd = "viewTypeDetails";
      Action detailsAction = chooser.getActionMap().get(cmd);
      if (Objects.nonNull(detailsAction)) {
        ActionEvent ae = new ActionEvent(chooser, ActionEvent.ACTION_PERFORMED, cmd);
        detailsAction.actionPerformed(ae);
      }
      descendants(chooser)
          .filter(JTable.class::isInstance)
          .map(JTable.class::cast)
          .findFirst()
          .ifPresent(MainPanel::addCellEditorFocusListener);
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p1 = new JPanel();
    String title1 = "The cell editor selects the whole filename";
    p1.setBorder(BorderFactory.createTitledBorder(title1));
    p1.add(button1);

    JPanel p2 = new JPanel();
    String title2 = "The cell editor selects the filename without the extension";
    p2.setBorder(BorderFactory.createTitledBorder(title2));
    p2.add(button2);
    p2.add(button3);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(p1);
    p.add(p2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  private static void selectWithoutExtension(JTextField editor) {
    EventQueue.invokeLater(() -> {
      String name = editor.getText();
      int end = name.lastIndexOf('.');
      editor.setSelectionStart(0);
      editor.setSelectionEnd(end > 0 ? end : name.length());
    });
  }

  private static void addCellEditorListener(JList<?> list) {
    boolean readOnly = UIManager.getBoolean("FileChooser.readOnly");
    if (!readOnly) {
      list.addContainerListener(new ContainerAdapter() {
        @Override public void componentAdded(ContainerEvent e) {
          Component c = e.getChild();
          if (c instanceof JTextField && "Tree.cellEditor".equals(c.getName())) {
            selectWithoutExtension((JTextField) c);
          }
        }
      });
    }
  }

  private static void addCellEditorFocusListener(JTable table) {
    boolean readOnly = UIManager.getBoolean("FileChooser.readOnly");
    TableColumnModel columnModel = table.getColumnModel();
    if (!readOnly && columnModel.getColumnCount() > 0) {
      TableColumn tc = columnModel.getColumn(0);
      DefaultCellEditor editor = (DefaultCellEditor) tc.getCellEditor();
      JTextField tf = (JTextField) editor.getComponent();
      tf.addFocusListener(new FocusAdapter() {
        @Override public void focusGained(FocusEvent e) {
          selectWithoutExtension(tf);
        }
      });
    }
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
