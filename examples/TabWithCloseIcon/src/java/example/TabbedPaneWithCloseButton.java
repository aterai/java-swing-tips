// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class TabbedPaneWithCloseButton extends JTabbedPane {
  private List<JButton> closeButtons;

  @Override public void updateUI() {
    Optional.ofNullable(closeButtons).ifPresent(list -> {
      list.forEach(this::remove);
      list.clear();
    });
    super.updateUI();
    closeButtons = new ArrayList<>();
    setUI(new CloseButtonTabbedPaneUI(closeButtons));
  }
}

// Copied from
// JTabbedPane with close Icons | Oracle Forums
// https://community.oracle.com/thread/1356993
class CloseButtonTabbedPaneUI extends BasicTabbedPaneUI {
  public final List<JButton> closeButtons; // = new ArrayList<>();

  protected CloseButtonTabbedPaneUI(List<JButton> closeButtons) {
    super();
    this.closeButtons = closeButtons;
  }

  @Override protected LayoutManager createLayoutManager() {
    return new CloseButtonTabbedPaneLayout();
  }

  // add 40 to the tab size to allow room for the close button and 2 + 2 to the height
  @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) {
    // note that the insets that are returned to us are not copies.
    Insets defaultInsets = (Insets) super.getTabInsets(tabPlacement, tabIndex).clone();
    defaultInsets.right += 40;
    defaultInsets.top += 2;
    defaultInsets.bottom += 2;
    return defaultInsets;
  }

  private final class CloseButtonTabbedPaneLayout extends TabbedPaneLayout {
    // a list of our close buttons
    @Override public void layoutContainer(Container parent) {
      super.layoutContainer(parent);
      // ensure that there are at least as many close buttons as tabs
      while (tabPane.getTabCount() > closeButtons.size()) {
        closeButtons.add(createTabCloseButton(tabPane, closeButtons.size()));
      }
      Rectangle r = new Rectangle();
      int tabPlacement = tabPane.getTabPlacement();
      int i;
      for (i = 0; i < tabPane.getTabCount(); i++) {
        getTabBounds(i, r);
        JButton closeButton = closeButtons.get(i);
        Dimension d = closeButton.getPreferredSize();
        boolean isSelected = i == tabPane.getSelectedIndex();
        int x = getTabLabelShiftX(tabPlacement, i, isSelected) + r.x + r.width - d.width - 2;
        int y = getTabLabelShiftY(tabPlacement, i, isSelected) + r.y + (r.height - d.height) / 2;
        closeButton.setBounds(x, y, d.width, d.height);
        tabPane.add(closeButton);
      }
      for (; i < closeButtons.size(); i++) {
        // remove any extra close buttons
        tabPane.remove(closeButtons.get(i));
      }
    }
  }

  protected static JButton createTabCloseButton(JTabbedPane tabbedPane, int index) {
    return new CloseButton(tabbedPane, index);
  }
}

class CloseButton extends JButton implements UIResource {
  private transient MouseAdapter handler;

  protected CloseButton(JTabbedPane tabPane, int index) {
    super(new CloseButtonAction(tabPane, index));
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    setToolTipText("Close this tab");
    // setMargin(new Insets(0, 0, 0, 0));
    setBorder(BorderFactory.createEmptyBorder());
    setFocusPainted(false);
    setBorderPainted(false);
    setContentAreaFilled(false);
    setRolloverEnabled(false);
    handler = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        setForeground(Color.RED);
      }

      @Override public void mouseExited(MouseEvent e) {
        setForeground(Color.BLACK);
      }
    };
    addMouseListener(handler);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(16, 16);
  }
}

class CloseButtonAction extends AbstractAction {
  private final JTabbedPane tabPane;
  private final int index;

  protected CloseButtonAction(JTabbedPane tabPane, int index) {
    super("x");
    this.tabPane = tabPane;
    this.index = index;
  }

  @Override public void actionPerformed(ActionEvent e) {
    tabPane.remove(index);
  }
}
