package menader.util;

public class XPath {

  public static final String DESCENDANT = "descendant::";
  public static final String DESCENDANT_OR_SELF = "descendant-or-self::";
  public static final String ANCESTOR = "ancestor::";
  public static final String ANCESTOR_OR_SELF = "ancestor-or-self::";
  public static final String ATTRIBUTE = "attribute::";
  public static final String CHILD = "child::";
  public static final String NAMESPACE = "namespace::";
  public static final String SELF = "self";
  public static final String PARENT = "parent";
  public static final String FOLLOWING = "following::";
  public static final String FOLLOWING_SIBLING = "following-sibling::";
  public static final String PRECEDING = "preceding::";
  public static final String PRECEDING_SIBLING = "preceding-sibling::";

  public static String descendantOrSelf(String elem) {
    return DESCENDANT_OR_SELF + elem;
  }
}
