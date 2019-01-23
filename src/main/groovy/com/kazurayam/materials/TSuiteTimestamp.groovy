package com.kazurayam.materials

import java.time.LocalDateTime

/**
 * Wraps a time stamp when a Test Suite was executed.
 * The time stamp value is formatted in 'yyyyMMdd_HHmmss'
 *
 * @author kazurayam
 *
 */
interface TSuiteTimestamp extends Comparable<TSuiteTimestamp> {
    
    static final String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'
    
    LocalDateTime getValue()

    /**
     *
     * @return String in the 'yyyyMMdd_HHmmss' format
     */
    String format()

    String toJson()
    
}