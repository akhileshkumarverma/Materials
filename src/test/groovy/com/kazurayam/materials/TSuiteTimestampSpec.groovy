package com.kazurayam.materials

import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

/**
 * TestSuite Timestamp
 *
 * @author kazurayam
 *
 */
//@Ignore
class TSuiteTimestampSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteTimestampSpec);

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testParse() {
        setup:
        String fixture = '20180529_143459'
        LocalDateTime expected = LocalDateTime.of(2018, 5, 29, 14, 34, 59)
        when:
        LocalDateTime actual = TSuiteTimestamp.parse(fixture)
        then:
        actual == expected
    }

    def testEquals() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 44)
        LocalDateTime expected1 = LocalDateTime.of(2018, 5, 29, 11, 22, 33)
        LocalDateTime expected2 = LocalDateTime.of(2018, 5, 29, 11, 22, 33, 00)
        when:
        TSuiteTimestamp ts = new TSuiteTimestamp(source)
        then:
        ts.getValue() == expected1
        ts.getValue() == expected2
        //cleanup:
    }

    def testToJson() {
        setup:
        LocalDateTime source = LocalDateTime.of(2018, 6, 5, 9, 2, 13)
        when:
        TSuiteTimestamp ts = new TSuiteTimestamp(source)
        def str = ts.toString()
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(str)}")
        then:
        str.contains('{"TSuiteTimestamp":')
        str.contains('{"timestamp":')
        str.contains('20180605_090213')
        str.contains('}}')
    }

    def testFormatOfTimeless() {
        expect:
        TSuiteTimestamp.TIMELESS.format() == TSuiteTimestamp.TIMELESS_DIRNAME
    }

    def testParseDirnameOfTimeless() {
        expect:
        TSuiteTimestamp.parse(TSuiteTimestamp.TIMELESS_DIRNAME) == LocalDateTime.MIN
    }

    // helper methods
}
