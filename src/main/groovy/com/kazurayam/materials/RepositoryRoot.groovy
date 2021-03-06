package com.kazurayam.materials

import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RepositoryRoot {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRoot.class)

    private Path baseDir_
    private List<TSuiteResult> tSuiteResults_

    RepositoryRoot(Path baseDir) {
        assert baseDir != null
        Helpers.ensureDirs(baseDir)
        baseDir_ = baseDir
        tSuiteResults_ = new ArrayList<TSuiteResult>()
    }

    // ------------------- getter -------------------------------------------
    Path getBaseDir() {
        return baseDir_
    }

    // ------------------- child nodes operation ----------------------------
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        boolean found = false
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            tSuiteResults_.add(tSuiteResult)
            Collections.sort(tSuiteResults_)
        }
    }

    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    List<TSuiteResult> getTSuiteResults(TSuiteName tSuiteName) {
        List<TSuiteResult> result = new ArrayList<TSuiteResult>()
        logger_.debug("#getTSuiteResults tSuiteResults_.size()=${tSuiteResults_.size()}")
        for (TSuiteResult tsr : tSuiteResults_) {
            logger_.debug("#getTSuiteResults tsr.getTSuiteName()=${tsr.getTSuiteName()}")
            if (tSuiteName == tsr.getTSuiteName()) {
                result.add(tsr)
            }
        }
        return result
    }

    List<TSuiteResult> getTSuiteResults() {
        return tSuiteResults_
    }

    /**
     * returns the sorted list of TSuiteResults ordered by
     * (1) TSuiteName in natural order
     * (2) TSuiteTimestamp in the reverse order
     *
     * @return
     */
    List<TSuiteResult> getSortedTSuiteResults() {
        Comparator<TSuiteResult> comparator = new Comparator<TSuiteResult>() {
            @Override
            public int compare(TSuiteResult o1, TSuiteResult o2) {
                int v = o1.getTSuiteName().compareTo(o2.getTSuiteName())
                if (v < 0) {
                    return v // natural order of TSuiteName
                } else if (v == 0) {
                    LocalDateTime ldt1 = o1.getTSuiteTimestamp().getValue()
                    LocalDateTime ldt2 = o2.getTSuiteTimestamp().getValue()
                    return ldt1.compareTo(ldt2) * -1  // reverse order of TSuiteTimestamp
                } else {
                    return v  // natural order of TSuiteName
                }
            }
        }
        List<TSuiteResult> sorted = tSuiteResults_
        Collections.sort(sorted, comparator)
        return sorted
    }

    TSuiteResult getLatestModifiedTSuiteResult() {
        LocalDateTime lastModified = LocalDateTime.MIN
        TSuiteResult result = null
        List<TSuiteResult> tSuiteResults = this.getTSuiteResults()
        for (TSuiteResult tsr : tSuiteResults) {
            if (tsr.getLastModified() > lastModified) {
                result = tsr
                lastModified = tsr.getLastModified()
            }
        }
        return result
    }

    List<Material> getMaterials() {
        List<Material> list = new ArrayList<Material>()
        for (TSuiteResult tsr : this.getSortedTSuiteResults()) {
            List<Material> mates = tsr.getMaterials()
            for (Material mate : mates) {
                list.add(mate)
            }
        }
        return list
    }



    // -------------- overriding java.lang.Object methods ---------------------
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof RepositoryRoot)) { return false }
        RepositoryRoot other = (RepositoryRoot)obj
        List<TSuiteResult> ownList = this.getTSuiteResults()
        List<TSuiteResult> otherList = other.getTSuiteResults()
        logger_.debug("ownList=${ownList.toString()}")
        logger_.debug("otherList=${otherList.toString()}")
        if (ownList.size() == otherList.size()) {
            for (int i; i < ownList.size(); i++) {
                if (ownList.get(i) != otherList.get(i)) { return false }
            }
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        return com.kazurayam.materials.RepositoryRoot.class.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryRoot":{')
        sb.append('"tSuiteResults":[')
        def count = 0
        for (TSuiteResult tsr : tSuiteResults_) {
            if (count > 0) { sb.append(',') }
            count += 1
            sb.append(tsr.toJson())
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}
