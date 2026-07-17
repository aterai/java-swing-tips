// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    // Tab1: Removes selected items one at a time via DefaultListModel#remove(int).
    tabs.addTab("default remove", createIndividualRemovalTab());

    // Tab2: Collects the unselected elements and swaps in a brand-new model.
    tabs.addTab("clear + addElement", createModelRebuildTab());

    // Tab3: Uses a custom ListModel that batches removal into fewer events.
    tabs.addTab("addAll + remove", createBatchArrayModelTab());
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  /**
   * Individual-removal strategy: removes each selected index one at a time,
   * from the highest index down (so earlier removals don't shift the
   * remaining target indices). This fires one {@code fireIntervalRemoved}
   * event per removed element, so repaint/revalidate cost grows with the
   * number of selected items.
   */
  private static <E> void transferSelectedByIndividualRemoval(JList<E> src, JList<E> dst) {
    int[] selectedIndices = src.getSelectedIndices();
    if (selectedIndices.length > 0) {
      DefaultListModel<E> sourceModel = (DefaultListModel<E>) src.getModel();
      DefaultListModel<E> destinationModel = (DefaultListModel<E>) dst.getModel();
      for (int index : selectedIndices) {
        destinationModel.addElement(sourceModel.get(index));
      }
      // Remove from the tail first so lower indices stay valid.
      for (int i = selectedIndices.length - 1; i >= 0; i--) {
        sourceModel.remove(selectedIndices[i]);
      }
    }
  }

  /**
   * Model-rebuild strategy: collects the unselected elements in a single
   * O(n) pass and swaps in a fresh model via {@code setModel}. Only one
   * structural change is signaled, so this scales much better than
   * per-item removal for large lists (tens of thousands of items).
   * The trade-off is allocating a brand-new model object on every move.
   */
  private static <E> void transferSelectedByModelRebuild(JList<E> src, JList<E> dst) {
    int[] selectedIndices = src.getSelectedIndices();
    if (selectedIndices.length > 0) {
      DefaultListModel<E> sourceModel = (DefaultListModel<E>) src.getModel();
      DefaultListModel<E> destinationModel = (DefaultListModel<E>) dst.getModel();
      ListSelectionModel selectionModel = src.getSelectionModel();
      List<E> unselectedValues = new ArrayList<>();
      for (int i = 0; i < sourceModel.getSize(); i++) {
        if (!selectionModel.isSelectedIndex(i)) {
          unselectedValues.add(sourceModel.getElementAt(i));
        }
      }
      for (int index : selectedIndices) {
        destinationModel.addElement(sourceModel.get(index));
      }
      DefaultListModel<E> rebuiltModel = new DefaultListModel<>();
      unselectedValues.forEach(rebuiltModel::addElement);

      // // Java 11:
      // // https://bugs.openjdk.org/browse/JDK-8201289
      // // Destination is a live model: batch the insert so it fires O(1)
      // // events instead of one per selected element.
      // destinationModel.addAll(src.getSelectedValuesList());
      // DefaultListModel<E> rebuiltModel = new DefaultListModel<>();
      // rebuiltModel.addAll(unselectedValues);

      src.setModel(rebuiltModel);
    }
  }

  // /**
  //  * Model-rebuild strategy: collects the unselected elements in a single
  //  * O(n) pass and swaps in a fresh model via {@code setModel} on the
  //  * {@code src} side. That swap fires only one structural event since
  //  * {@code rebuiltModel} has no listeners yet (it isn't attached to any
  //  * JList until {@code setModel} runs). The trade-off is allocating a
  //  * brand-new model object on every move.
  //  * <p>
  //  * Note: the {@code destination} model is different — it's already live,
  //  * attached to a visible JList. Adding to it one element at a time (the
  //  * original implementation) fires one {@code ListDataEvent} per element,
  //  * and each event triggers JList's internal bookkeeping (e.g. adjusting
  //  * the selection model's index ranges). When the selection is a large
  //  * fraction of the list — e.g. selecting nearly all 20,000 items and
  //  * moving them — that per-element cost accumulates and dominates the
  //  * whole operation. Batching the destination-side insert via
  //  * {@link DefaultListModel#addAll(java.util.Collection)} collapses those
  //  * events from O(k) down to O(1), which is the fix applied below.
  //  * [JDK-8201289] DefaultListModel and DefaultComboBoxModel
  //  *    should support addAll (Collection c) - Java Bug System
  //  * https://bugs.openjdk.org/browse/JDK-8201289
  //  */
  // private static <E> void transferSelectedByModelRebuild(JList<E> src, JList<E> dst) {
  //   if (!src.isSelectionEmpty()) {
  //     ListSelectionModel selectionModel = src.getSelectionModel();
  //     DefaultListModel<E> sourceModel = (DefaultListModel<E>) src.getModel();
  //     DefaultListModel<E> destinationModel = (DefaultListModel<E>) dst.getModel();
  //
  //     List<E> unselectedValues = new ArrayList<>();
  //     for (int i = 0; i < sourceModel.getSize(); i++) {
  //       if (!selectionModel.isSelectedIndex(i)) {
  //         unselectedValues.add(sourceModel.getElementAt(i));
  //       }
  //     }
  //     // Destination is a live model: batch the insert so it fires O(1)
  //     // events instead of one per selected element.
  //     destinationModel.addAll(src.getSelectedValuesList());
  //
  //     DefaultListModel<E> rebuiltModel = new DefaultListModel<>();
  //     rebuiltModel.addAll(unselectedValues);
  //     src.setModel(rebuiltModel);
  //   }
  // }

  /**
   * Batch-array-model strategy: uses the custom {@link ArrayListModel}'s
   * {@code addAll}/{@code remove(int...)}, which each fire a bounded
   * number of events instead of one per element. Avoids reallocating a
   * whole new model on every move, so it uses less memory than the
   * rebuild strategy while still batching UI notifications.
   */
  private static <E> void transferSelectedByBatchArrayModel(JList<E> src, JList<E> dst) {
    int[] selectedIndices = src.getSelectedIndices();
    if (selectedIndices.length > 0) {
      ((ArrayListModel<E>) dst.getModel()).addAll(src.getSelectedValuesList());
      ((ArrayListModel<E>) src.getModel()).remove(selectedIndices);
    }
  }

  private static <E> JList<E> createSelectableList(ListModel<E> model) {
    JList<E> list = new JList<>(model);
    JPopupMenu popup = new JPopupMenu();
    // Right-click menu action that inverts the current selection.
    popup.add("reverse").addActionListener(e -> {
      ListSelectionModel selectionModel = list.getSelectionModel();
      for (int i = 0; i < list.getModel().getSize(); i++) {
        if (selectionModel.isSelectedIndex(i)) {
          selectionModel.removeSelectionInterval(i, i);
        } else {
          selectionModel.addSelectionInterval(i, i);
        }
      }
    });
    // Selects every other item (worst case for range-based removal).
    popup.add("alternate").addActionListener(e -> {
      ListSelectionModel selectionModel = list.getSelectionModel();
      for (int i = 0; i < list.getModel().getSize(); i++) {
        if (i % 2 == 0) {
          selectionModel.addSelectionInterval(i, i);
        }
      }
    });
    list.setComponentPopupMenu(popup);
    return list;
  }

  private static JButton createTransferButton(String title) {
    JButton button = new JButton(title);
    button.setFocusable(false);
    button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    return button;
  }

  private Component createIndividualRemovalTab() {
    DefaultListModel<String> model = new DefaultListModel<>();
    IntStream.range(0, 5000).mapToObj(Objects::toString).forEach(model::addElement);
    JList<String> leftList = createSelectableList(model);
    JList<String> rightList = createSelectableList(new DefaultListModel<>());

    JButton moveRightButton = createTransferButton(">");
    moveRightButton.addActionListener(e ->
        transferSelectedByIndividualRemoval(leftList, rightList));

    JButton moveLeftButton = createTransferButton("<");
    moveLeftButton.addActionListener(e ->
        transferSelectedByIndividualRemoval(rightList, leftList));

    return SpringLayoutUtils.createDualListPanel(
        leftList, rightList, moveRightButton, moveLeftButton);
  }

  private Component createModelRebuildTab() {
    DefaultListModel<String> model = new DefaultListModel<>();
    IntStream.range(10_000, 30_000).mapToObj(Objects::toString).forEach(model::addElement);
    JList<String> leftList = createSelectableList(model);
    JList<String> rightList = createSelectableList(new DefaultListModel<>());

    JButton moveRightButton = createTransferButton(">");
    moveRightButton.addActionListener(e ->
        transferSelectedByModelRebuild(leftList, rightList));

    JButton moveLeftButton = createTransferButton("<");
    moveLeftButton.addActionListener(e ->
        transferSelectedByModelRebuild(rightList, leftList));

    return SpringLayoutUtils.createDualListPanel(
        leftList, rightList, moveRightButton, moveLeftButton);
  }

  private Component createBatchArrayModelTab() {
    ArrayListModel<String> model = new ArrayListModel<>();
    IntStream.range(30_000, 50_000).mapToObj(Objects::toString).forEach(model::add);
    JList<String> leftList = createSelectableList(model);
    JList<String> rightList = createSelectableList(new ArrayListModel<>());

    JButton moveRightButton = createTransferButton(">");
    moveRightButton.addActionListener(e ->
        transferSelectedByBatchArrayModel(leftList, rightList));

    JButton moveLeftButton = createTransferButton("<");
    moveLeftButton.addActionListener(e ->
        transferSelectedByBatchArrayModel(rightList, leftList));

    return SpringLayoutUtils.createDualListPanel(
        leftList, rightList, moveRightButton, moveLeftButton);
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

/**
 * A lightweight {@link ListModel} backed by an {@link ArrayList}.
 * {@code addAll}/{@code remove(int...)} add or remove several elements
 * at once while only firing events for the ranges that actually changed,
 * keeping the number of notifications low without misreporting them.
 */
class ArrayListModel<E> extends AbstractListModel<E> {
  private final List<E> items = new ArrayList<>();

  public void add(E element) {
    int index = items.size();
    items.add(element);
    fireIntervalAdded(this, index, index);
  }

  public void addAll(Collection<? extends E> elements) {
    int firstAddedIndex = items.size();
    items.addAll(elements);
    int lastAddedIndex = items.size() - 1;
    if (lastAddedIndex >= firstAddedIndex) {
      // Notify only the actually-appended range.
      fireIntervalAdded(this, firstAddedIndex, lastAddedIndex);
    }
  }

  public E remove(int index) {
    E removedElement = items.get(index);
    items.remove(index);
    fireIntervalRemoved(this, index, index);
    return removedElement;
  }

  public void remove(int... selectedIndices) {
    if (selectedIndices.length > 0) {
      // JList re-reads values from the model when it repaints, so the displayed
      // content stays correct even though the notified range doesn't
      // exactly match the literal set of removed positions.
      int max = selectedIndices.length - 1;
      for (int i = max; i >= 0; i--) {
        items.remove(selectedIndices[i]);
      }
      fireIntervalRemoved(this, selectedIndices[0], selectedIndices[max]);
    }
  }

  @Override public E getElementAt(int index) {
    return items.get(index);
  }

  @Override public int getSize() {
    return items.size();
  }
}

final class SpringLayoutUtils {
  private SpringLayoutUtils() {
    /* Singleton */
  }

  private static void setScaleAndAdd(
      Container parent, Component child, Rectangle2D bounds) {
    LayoutManager layoutManager = parent.getLayout();
    if (layoutManager instanceof SpringLayout) {
      SpringLayout layout = (SpringLayout) layoutManager;
      Spring parentWidth = layout.getConstraint(SpringLayout.WIDTH, parent);
      Spring parentHeight = layout.getConstraint(SpringLayout.HEIGHT, parent);
      SpringLayout.Constraints childConstraints = layout.getConstraints(child);
      childConstraints.setX(Spring.scale(parentWidth, (float) bounds.getX()));
      childConstraints.setY(Spring.scale(parentHeight, (float) bounds.getY()));
      childConstraints.setWidth(Spring.scale(parentWidth, (float) bounds.getWidth()));
      childConstraints.setHeight(Spring.scale(parentHeight, (float) bounds.getHeight()));
      parent.add(child);
    }
  }

  public static Component createDualListPanel(
      JList<?> leftList, JList<?> rightList, JButton moveRightBtn, JButton moveLeftBtn) {
    Box buttonBox = Box.createVerticalBox();
    buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    buttonBox.add(Box.createVerticalGlue());
    buttonBox.add(moveRightBtn);
    buttonBox.add(Box.createVerticalStrut(20));
    buttonBox.add(moveLeftBtn);
    buttonBox.add(Box.createVerticalGlue());

    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.add(buttonBox);

    JScrollPane leftScroll = new JScrollPane(leftList);
    JScrollPane rightScroll = new JScrollPane(rightList);

    JPanel p = new JPanel(new SpringLayout());
    setScaleAndAdd(p, leftScroll, new Rectangle2D.Float(.05f, .05f, .40f, .90f));
    setScaleAndAdd(p, buttonPanel, new Rectangle2D.Float(.45f, .05f, .10f, .90f));
    setScaleAndAdd(p, rightScroll, new Rectangle2D.Float(.55f, .05f, .40f, .90f));
    return p;
  }
}
