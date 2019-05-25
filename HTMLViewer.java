import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

class HTMLViewer {
  JFrame jfrm;
  JTree jtree;
  JScrollPane jscroll;
  DefaultMutableTreeNode top;

  HTMLViewer() {
    jfrm = new JFrame("HTMLViewer");
    top = new DefaultMutableTreeNode("html");
    jtree = new JTree(top);
    jscroll = new JScrollPane(jtree);

    jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jfrm.add(jscroll,BorderLayout.CENTER);
    jfrm.setSize(new Dimension(500,600));
    jfrm.setVisible(true);
  }

  public static void main(String[] args) {
    HTMLParser parser = new HTMLParser();

    String docu = "<!DOCTYPE html><html lang='en'><head><meta charset='utf-8' /><title>A title</title><script>var s = ' \' </head> \' ';alert(s);</script><script src='test.js' /></head><body><!-- some comment with <b> html inside --><h1>A header</h1><b><i>something</b> something </i> else<div><div></div><div></div></div><div></div><div><p>Some text in a <em>paragraph</em><p>Matthew's PB and J sandwich<br>hi there!<br><p><a href='someplace.htm'>a link</a> in a p tag</div><ul><li>thing one<li>thing two<li>thing three</ul><script src='something.js'></script></body></html>";

    parser.parse(docu);

    (new HTMLViewer()).addNodes(parser);

  }

  // helper to recursively add nodes to the JTree
  void addNodesHelper(HTMLElement elem, DefaultMutableTreeNode node) {
    for(int i = 0; i < elem.contents.size(); i++) {
      HTMLObject obj = elem.contents.get(i);
      if(obj.getClass().getName().equals("HTMLElement")) {
        DefaultMutableTreeNode newNode =
          new DefaultMutableTreeNode(obj.toHTML());
        node.add(newNode);
        addNodesHelper((HTMLElement) obj, newNode);
      } else {
        if(!obj.toHTML().trim().equals("")) {
          node.add(
            new DefaultMutableTreeNode(
              obj.toHTML()));
        }
      }
    }
  }

  // add nodes to the JTree
  void addNodes(HTMLParser parser) {
    addNodesHelper(parser.rootElement,top);
  }
}
