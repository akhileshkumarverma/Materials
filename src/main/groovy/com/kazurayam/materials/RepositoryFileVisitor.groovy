package com.kazurayam.materials

import static java.nio.file.FileVisitResult.*

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class RepositoryFileVisitor extends SimpleFileVisitor<Path> {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryFileVisitor.class)

    private RepositoryRoot repoRoot_

    private TSuiteName tSuiteName_
    private TSuiteTimestamp tSuiteTimestamp_
    private TSuiteResult tSuiteResult_
    private TCaseName tCaseName_
    private TCaseResult tCaseResult_
    private Material material_

    private static enum Layer {
        INIT, ROOT, TESTSUITE, TIMESTAMP, TESTCASE, SUBDIR
    }
    private int subdirDepth_ = 0
    private Stack<Layer> directoryTransition_

    RepositoryFileVisitor(RepositoryRoot repoRoot) {
        repoRoot_ = repoRoot
        directoryTransition_ = new Stack<Layer>()
        directoryTransition_.push(Layer.INIT)
    }

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        def from = directoryTransition_.peek()
        switch (from) {
            case Layer.INIT :
                logger_.debug("#preVisitDirectory visiting ${dir} as ROOT")
                directoryTransition_.push(Layer.ROOT)
                break
            case Layer.ROOT :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTSUITE")
                tSuiteName_ = new TSuiteName(dir)
                directoryTransition_.push(Layer.TESTSUITE)
                break
            case Layer.TESTSUITE:
                logger_.debug("#preVisitDirectory visiting ${dir} as TIMESTAMP")
                LocalDateTime ldt = TSuiteTimestamp.parse(dir.getFileName().toString())
                if (ldt != null) {
                    tSuiteTimestamp_ = new TSuiteTimestamp(ldt)
                    tSuiteResult_ = new TSuiteResult(tSuiteName_, tSuiteTimestamp_).setParent(repoRoot_)
                    repoRoot_.addTSuiteResult(tSuiteResult_)
                } else {
                    logger_.warn("#preVisitDirectory ${dir} is ignored, as it's fileName '${dir.getFileName()}' is not compliant to" +
                            " the TSuiteTimestamp format (${TSuiteTimestamp.DATE_TIME_PATTERN})")
                }
                directoryTransition_.push(Layer.TIMESTAMP)
                break
            case Layer.TIMESTAMP :
                logger_.debug("#preVisitDirectory visiting ${dir} as TESTCASE")
                tCaseName_ = new TCaseName(dir)
                tCaseResult_ = tSuiteResult_.getTCaseResult(tCaseName_)
                if (tCaseResult_ == null) {
                    tCaseResult_ = new TCaseResult(tCaseName_).setParent(tSuiteResult_)
                    tSuiteResult_.addTCaseResult(tCaseResult_)
                }
                directoryTransition_.push(Layer.TESTCASE)
                break
            case Layer.TESTCASE :
                logger_.debug("#preVisitDirectory visiting ${dir} as SUBDIR(${subdirDepth_})")
                //
                subdirDepth_ += 1
                directoryTransition_.push(Layer.SUBDIR)
                break
            case Layer.SUBDIR :
                logger_.debug("#preVisitDirectory visiting ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ += 1
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
        def to = directoryTransition_.peek()
        switch (to) {
            case Layer.SUBDIR :
                logger_.debug("#postVisitDirectory leaving ${dir} as SUBDIR(${subdirDepth_})")
                subdirDepth_ -= 1
                if (subdirDepth_ == 0) {
                    directoryTransition_.pop()
                }
                break
            case Layer.TESTCASE :
                logger_.debug("#postVisitDirectory leaving ${dir} as TESTCASE")
                // resolve the lastModified property of the TCaseResult
                LocalDateTime lastModified = resolveLastModifiedOfTCaseResult(tCaseResult_)
                tCaseResult_.setLastModified(lastModified)
                logger_.debug("#postVisitDirectory set lastModified=${lastModified} to ${tCaseResult_.getTCaseName()}")
                directoryTransition_.pop()
                break
            case Layer.TIMESTAMP :
                logger_.debug("#postVisitDirectory leaving ${dir} as TIMESTAMP")
                // resolve the lastModified property of the TSuiteResult
                LocalDateTime lastModified = resolveLastModifiedOfTSuiteResult(tSuiteResult_)
                tSuiteResult_.setLastModified(lastModified)
                logger_.debug("#postVisitDirectory set lastModified=${lastModified} to" +
                    " ${tSuiteResult_.getTSuiteName()}/${tSuiteResult_.getTSuiteTimestamp().format()}")
                directoryTransition_.pop()
                break
            case Layer.TESTSUITE :
                logger_.debug("#postVisitDirectory leaving ${dir} as TESTSUITE")
                directoryTransition_.pop()
                break
            case Layer.ROOT :
                logger_.debug("#postVisitDirectory leaving ${dir} as ROOT")
                directoryTransition_.pop()
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a file in a directory.
     */
    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        switch (directoryTransition_.peek()) {
            case Layer.ROOT :
                logger_.debug("#visitFile ${file} in ROOT; this file is ignored")
                break
            case Layer.TESTSUITE :
                logger_.debug("#visitFile ${file} in TESTSUITE; this file is ignored")
                break
            case Layer.TIMESTAMP :
                logger_.debug("#visitFile ${file} in TIMESTAMP; this file is ignored")
                break
            case Layer.TESTCASE :
            case Layer.SUBDIR :
                Material material = new Material(tCaseResult_, file)
                material.setLastModified(file.toFile().lastModified())
                tCaseResult_.addMaterial(material)
                logger_.debug("#visitFile ${file} in TESTCASE, tCaseResult=${tCaseResult_.toString()}")
                break
        }
        return CONTINUE
    }

    /**
     * Invoked for a file that could not be visited.
     *
     @Override
      FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {}
     */



    // helpers

    /**
     *
     * @param a instance of TCaseResult
     * @return LocalDateTime for TCaseResult's lastModified property
     */
    LocalDateTime resolveLastModifiedOfTCaseResult(TCaseResult tcr) {
        LocalDateTime lastModified = LocalDateTime.MIN
        List<Material> materials = tcr.getMaterials()
        for (Material mate : materials) {
            if (mate.getLastModified() > lastModified) {
                lastModified = mate.getLastModified()
            }
        }
        return lastModified
    }

    /**
     *
     * @param an instance of TSuiteResult
     * @return LocalDateTime for TSuiteResutl's lastModified property
     */
    LocalDateTime resolveLastModifiedOfTSuiteResult(TSuiteResult tsr) {
        LocalDateTime lastModified = LocalDateTime.MIN
        List<TCaseResult> tCaseResults = tsr.getTCaseResults()
        for (TCaseResult tcr : tCaseResults) {
            if (tcr.getLastModified() > lastModified) {
                lastModified = tcr.getLastModified()
            }
        }
        return lastModified
    }

}
