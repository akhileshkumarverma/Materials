package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

final class ScreenshotRepository {

    private Path baseDir
    private Map<String, Map<TSTimestamp, TestSuiteResult>> testSuiteResults

    private TSTimestamp currentTimestamp
    private String currentTestSuiteId
    private String currentTestCaseId

    /**
     *
     * @param basDir
     */
    ScreenshotRepository(Path baseDir) {
        this.init(baseDir)
    }

    /**
     *
     * @param basDir
     */
    ScreenshotRepository(String baseDirString) {
        Path baseDir = Paths.get(System.getProperty('user.dir')).resolve(baseDirString)
        this.init(baseDir)
    }

    /**
     * You are supposed to call this in the TestListener@BeforeTestSuite as follows:
     *
     * <PRE>
     * import java.nio.file.Path
     * import java.nio.file.Paths
     * import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
     * import katalonimport com.kms.katalon.core.configuration.RunConfiguration
     * import com.kazurayam.ksbackyard.screenshotsupport.ScreenshotRepository
     * ...
     * class TL {
     *     @BeforeTestSuite
     *     def beforeTestSuite(TestSuiteContext testSuiteContext) {
     *         ScreenshotRepository scRepos =
     *             new ScreenshotRepository('Screenshots', testSuiteContext.getTestSuiteId())
     *         GlobalVariable.SCREENSHOT_REPOSITORY = scRepos
     *         WebUI.comment(">>> got instance of ${scRepos.toString()}")
     *     }
     * </PRE>
     *
     * @param dirPath directory under which a directory named as BASE_DIR_NAME will be created.
     */
    ScreenshotRepository(String baseDirString, String testSuiteId) {
        this(baseDirString)
        this.currentTestSuiteId = testSuiteId
        this.currentTimestamp = new TSTimestamp()
        this.findOrNewTestSuiteResult(this.currentTestSuiteId, this.currentTimestamp)
    }

    ScreenshotRepository(Path baseDir, String testSuiteId) {
        this(baseDir)
        this.currentTestSuiteId = testSuiteId
        this.currentTimestamp = new TSTimestamp()
        this.findOrNewTestSuiteResult(this.currentTestSuiteId, this.currentTimestamp)
    }

    /**
     *
     * @param baseDir
     */
    private void init(Path baseDir) {
        this.baseDir = baseDir
        this.testSuiteResults = new HashMap<String, Map<TSTimestamp, TestSuiteResult>>()
        loadTree(this.baseDir, this.testSuiteResults)
    }

    /**
     *
     * @param baseDir
     * @param tree
     */
    private void loadTree(Path baseDir, Map<String, Map<TSTimestamp, TestSuiteResult>> tree) {
        System.err.println("TODO")
    }

    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("${ScreenshotRepository.getName()}('${this.baseDir}')")
        return sb.toString()
    }

    Path getBaseDir() {
        return this.baseDir
    }

    void setCurrentTestSuiteId(String testSuiteId) {
       this.currentTestSuiteId = testSuiteId
    }

    String getCurrentTestSuiteId() {
        return this.currentTestSuiteId
    }

    void setCurrentTimestamp(TSTimestamp timestamp) {
        this.currentTimestamp = timestamp
    }

    TSTimestamp getCurrentTimestamp() {
        return this.currentTimestamp
    }

    void setCurrentTestCaseId(String testCaseId) {
        this.currentTestCaseId = testCaseId
    }

    String getCurrentTestCaseId() {
        return this.currentTestCaseId
    }

    void setCurrentTestCaseStatus(String testCaseStatus) {
        this.getCurrentTestCaseResult().setTestCaseStatus(testCaseStatus)
    }

    String getCurrentTestCaseStatus() {
        return this.getCurrentTestCaseResult().getTestCaseStatus()
    }

    TestSuiteResult getCurrentTestSuiteResult() {
        if (this.currentTestSuiteId != null) {
            if (this.currentTimestamp != null) {
                return findOrNewTestSuiteResult(this.currentTestSuiteId, this.currentTimestamp)
            } else {
                throw new IllegalStateException('currentTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTestSuiteId is not set')
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TestSuiteResult findOrNewTestSuiteResult(String testSuiteId, TSTimestamp timestamp) {
        TestSuiteResult tsr
        if (this.testSuiteResults.containsKey(testSuiteId)) {
            Map<TSTimestamp, TestSuiteResult> series = this.testSuiteResults.get(testSuiteId)
            if (series.containsKey(timestamp)) {
                tsr = series.get(timestamp)
            } else {
                tsr = new TestSuiteResult(this, testSuiteId, timestamp)
                series.put(timestamp, tsr)
            }
        } else {
            tsr = new TestSuiteResult(this, testSuiteId, timestamp)
            Map<TSTimestamp, TestSuiteResult> series = new HashMap<TSTimestamp, TestSuiteResult>()
            series.put(timestamp, tsr)
            this.testSuiteResults.put(testSuiteId, series)
        }
        return tsr
    }

    TestCaseResult getCurrentTestCaseResult() {
        if (currentTestCaseId) {
            return this.getCurrentTestSuiteResult().findOrNewTestCaseResult(currentTestCaseId)
        }
        else {
            throw new IllegalStateException("currentTestCaseId is null")
        }
    }
}