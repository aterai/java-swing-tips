package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.*;

public class JTabbedPaneWithCloseButton extends JTabbedPane {
    private List<JButton> closeButtons;
    @Override public void updateUI() {
        if (closeButtons != null) {
            for (JButton b: closeButtons) {
                remove(b);
            }
            closeButtons.clear();
        }
        super.updateUI();
        closeButtons = new ArrayList<JButton>();
        setUI(new CloseButtonTabbedPaneUI(closeButtons));
    }
}

//Copid from
//JTabbedPane with close Icons | Oracle Forums
//https://community.oracle.com/thread/1356993
class CloseButtonTabbedPaneUI extends BasicTabbedPaneUI {
    public final List<JButton> closeButtons; // = new ArrayList<>();
    public CloseButtonTabbedPaneUI(List<JButton> closeButtons) {
        super();
        this.closeButtons = closeButtons;
    }

    @Override protected LayoutManager createLayoutManager() {
        return new CloseButtonTabbedPaneLayout();
    }
    //add 40 to the tab size to allow room for the close button and 8 to the height
    @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        //note that the insets that are returned to us are not copies.
        Insets defaultInsets = (Insets) super.getTabInsets(tabPlacement, tabIndex).clone();
        defaultInsets.right  += 40;
        defaultInsets.top    += 4;
        defaultInsets.bottom += 4;
        return defaultInsets;
    }
    private class CloseButtonTabbedPaneLayout extends TabbedPaneLayout {
        //a list of our close buttons
        @Override public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            //ensure that there are at least as many close buttons as tabs
            while (tabPane.getTabCount() > closeButtons.size()) {
                closeButtons.add(new CloseButton(tabPane, closeButtons.size()));
            }
            Rectangle rect = new Rectangle();
            int i;
            for (i = 0; i < tabPane.getTabCount(); i++) {
                rect = getTabBounds(i, rect);
                JButton closeButton = closeButtons.get(i);
                //shift the close button 3 down from the top of the pane and 20 to the left
                closeButton.setLocation(rect.x + rect.width - 20, rect.y + 5);
                closeButton.setSize(15, 15);
                tabPane.add(closeButton);
            }
            for (; i < closeButtons.size(); i++) {
                //remove any extra close buttons
                tabPane.remove(closeButtons.get(i));
            }
        }
    }
}

class CloseButton extends JButton implements UIResource {
    public CloseButton(JTabbedPane tabPane, int index) {
        super(new CloseButtonAction(tabPane, index));
        setToolTipText("Close this tab");
        setMargin(new Insets(0, 0, 0, 0));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setForeground(Color.RED);
            }
            @Override public void mouseExited(MouseEvent e) {
                setForeground(Color.BLACK);
            }
        });
    }
}

class CloseButtonAction extends AbstractAction {
    private final JTabbedPane tabPane;
    private final int index;
    public CloseButtonAction(JTabbedPane tabPane, int index) {
        super("x");
        this.tabPane = tabPane;
        this.index = index;
    }
    @Override public void actionPerformed(ActionEvent e) {
        tabPane.remove(index);
    }
}
