package com.monitorjbl.json;

/**
 * Dictates the order in which to search for matches when serializing objects.
 */
public enum MatcherBehavior {
  /**
   * Check for matches on an class first before using the
   * current path matcher
   */
  CLASS_FIRST,

  /**
   * Check for matches against the current path first before
   * looking for a matcher on the class
   */
  PATH_FIRST
}
