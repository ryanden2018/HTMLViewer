import javax.swing.*;
import java.awt.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class HTMLViewer implements ActionListener {
  JFrame jfrm;
  JTree jtree;
  JScrollPane jscroll;
  DefaultMutableTreeNode top;
  Font font;
  static final String SEL_OPEN = "1";
  static final String SEL_OPEN_FROM_URL = "2";
  static final String SEL_EXIT = "3";

  HTMLViewer() {
    jfrm = new JFrame("HTMLViewer");
    top = new DefaultMutableTreeNode("<html>&lt;html&gt;</html>");
    jtree = new JTree(top);
    jscroll = new JScrollPane(jtree);
    font = new Font("monospaced",Font.PLAIN,16);
    jtree.setFont(font);

    // remove icons from JTree
    DefaultTreeCellRenderer cr = new DefaultTreeCellRenderer();
    cr.setOpenIcon(null);
    cr.setClosedIcon(null);
    cr.setLeafIcon(null);
    jtree.setCellRenderer(cr);

    // construct menus
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JMenuItem openItem = new JMenuItem("Open File...");
    JMenuItem openItemFromURL = new JMenuItem("Open File from URL...");
    JMenuItem exitItem = new JMenuItem("Exit");
    fileMenu.add(openItem);
    fileMenu.add(openItemFromURL);
    fileMenu.addSeparator();
    fileMenu.add(exitItem);

    openItem.addActionListener(this);
    openItem.setActionCommand(SEL_OPEN);
    openItemFromURL.addActionListener(this);
    openItemFromURL.setActionCommand(SEL_OPEN_FROM_URL);
    exitItem.addActionListener(this);
    exitItem.setActionCommand(SEL_EXIT);

    jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jfrm.add(jscroll,BorderLayout.CENTER);
    jfrm.setSize(new Dimension(500,600));
    jfrm.setJMenuBar(menuBar);
    jfrm.setVisible(true);
  }


  public void actionPerformed(ActionEvent e) {
    switch(e.getActionCommand()) {
    }
  }

  public static void main(String[] args) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");

    HTMLParser parser = new HTMLParser();

    String docu = "";
    try {
      docu = new String ( Files.readAllBytes( Paths.get("test.html")) );
    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
    
    
    parser.parse(docu);

    (new HTMLViewer()).addNodes(parser);
  }


  String transform(String text) {
    return "<HTML>" + text.replace("<","&lt;").replace(">","&gt;") + "</HTML>";
  }

  // helper to recursively add nodes to the JTree
  void addNodesHelper(HTMLElement elem, DefaultMutableTreeNode node) {
    for(int i = 0; i < elem.contents.size(); i++) {
      HTMLObject obj = elem.contents.get(i);
      if(obj.getClass().getName().equals("HTMLElement")) {
        DefaultMutableTreeNode newNode =
          new DefaultMutableTreeNode(transform(obj.toHTML()));
        node.add(newNode);
        addNodesHelper((HTMLElement) obj, newNode);
      } else {
        node.add(
          new DefaultMutableTreeNode(
            transform(obj.toHTML())));
      }
    }
  }

  // add nodes to the JTree
  void addNodes(HTMLParser parser) {
    top.removeAllChildren();
    top.setUserObject(transform(parser.rootElement.toHTML()));
    addNodesHelper(parser.rootElement,top);
    TreePath tp = new TreePath(top);
    jtree.expandPath(tp);
    jtree.collapsePath(tp);
  }
}
