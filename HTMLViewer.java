import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class HTMLViewer {
  JFrame jfrm;
  JTree jtree;
  JScrollPane jscroll;
  DefaultMutableTreeNode top;
  Font font;

  HTMLViewer() {
    jfrm = new JFrame("HTMLViewer");
    top = new DefaultMutableTreeNode("<html>&lt;html&gt;</html>");
    jtree = new JTree(top);
    jscroll = new JScrollPane(jtree);
    font = new Font("monospaced",Font.PLAIN,16);
    jtree.setFont(font);

    jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jfrm.add(jscroll,BorderLayout.CENTER);
    jfrm.setSize(new Dimension(500,600));
    jfrm.setVisible(true);
  }

  public static void main(String[] args) {
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
