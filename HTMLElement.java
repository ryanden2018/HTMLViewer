import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class HTMLElement extends HTMLObject {
  String tagName;
  Hashtable<String,String> attributes;
  Vector<HTMLObject> contents;

  static String[] selfClosingTags() {
    String result[] = {"area","base","br","col","embed","hr","img","input","link",
      "meta","param","source","track","wbr","command","keygen","menuitem"};
    return result;
  }

  static String[] reopenableTags() {
    String result[] = {"b","i","a","font","em","h1","h2","h3","h4","h5","h6",
      "pre","strong","u"};
    return result;
  }

  static String[] nestableTags() {
    String result[] = {"div"};
    return result;
  }

  // create HTMLElement from tag only
  HTMLElement(String tagName) {
    this.tagName = tagName;
    this.attributes = new Hashtable<String,String>();
    this.contents = new Vector<HTMLObject>();
  }

  // copy an HTMLElement without retaining the contents
  HTMLElement(HTMLElement oldElem) {
    this.tagName = oldElem.tagName;
    this.attributes = new Hashtable<String,String>();
    this.contents = new Vector<HTMLObject>();
    for(Enumeration<String> e = oldElem.attributes.keys(); e.hasMoreElements();) {
      String attr = e.nextElement();
      String val = oldElem.attributes.get(attr);
      this.attributes.put(attr,val);
    }
  }

  // set attributes of this object from the given string
  public void setAttributes(HTMLElement elem) {
    for(Enumeration<String> e = this.attributes.keys(); e.hasMoreElements();) {
      this.attributes.remove(e.nextElement());
    }

    for(Enumeration<String> e = elem.attributes.keys(); e.hasMoreElements();) {
      String attr = e.nextElement();
      String val = elem.attributes.get(attr);
      this.attributes.put(attr,val);
    }
  }

  // create an HTMLElement from an opening tag string
  public static HTMLElement createFromString(String str) {
    if(!str.matches("\\A<[a-zA-Z][^<>]*>\\z")) {
      throw new IllegalArgumentException("Cannot parse HTML string: " + str);
    }

    String str1 = str.substring(1,str.length()-1);

    Pattern tagPattern = Pattern.compile("\\A[a-zA-Z][a-zA-Z0-9]*\\/?\\b");
    Matcher tagMatch = tagPattern.matcher(str1);
    tagMatch.find();
    HTMLElement elem = new HTMLElement(tagMatch.group().toLowerCase());

    String str2 = tagPattern.split(str1+" ")[1].trim();

    while(true) {
      Pattern attrPattern = Pattern.compile("\\A[a-zA-Z][a-zA-Z0-9_:\\.-]*=");
      Matcher attrMatch = attrPattern.matcher(str2);
      if( !attrMatch.find() ) {
        break;
      }
      String attr = attrMatch.group();
      attr = attr.substring(0,attr.length()-1);

      str2 = attrPattern.split(str2+" ")[1].trim();

      Pattern valuePattern = Pattern.compile("\\A'[^']*'");
      Matcher valueMatch = valuePattern.matcher(str2);
      if(!valueMatch.find()) {
        valuePattern = Pattern.compile("\\A\"[^\"]*\"");
        valueMatch = valuePattern.matcher(str2);
        if( !valueMatch.find() ) {
          valuePattern = Pattern.compile("\\A[^\\s'\"]*\\b");
          valueMatch = valuePattern.matcher(str2);
          if( !valueMatch.find() ) {
            break;
          }
        }
      }

      String value = valueMatch.group();
      if((value.charAt(0)=='\'') || (value.charAt(0)=='"')) {
        value = value.substring(1,value.length()-1);
      }

      str2 = valuePattern.split(str2+" ")[1].trim();
      elem.attributes.put(attr,value);
    }


    return elem;
  }

  // return this element in HTML form
  public String toHTML() {
    String html = "<";
    html += this.tagName;
    for(Enumeration<String> e = this.attributes.keys(); e.hasMoreElements();) {
      String attr = e.nextElement();
      String val = this.attributes.get(attr).replaceAll("\"","&quot;");
      html += " " + attr + "=" + "\"" + val + "\"";
    }
    if(Arrays.asList(HTMLElement.selfClosingTags()).contains(this.tagName)) {
      html += " /";
    }
    html += ">";
    return html;
  }

  // extract the tag name from a string
  static String tagFromString(String str) {
    return (new HTMLElement(str.replaceAll("/",""))).tagName;
  }
}
