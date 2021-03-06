package com.kazurayam.materials

class IndexerFactory {

    static Indexer newIndexer() {
        //return new IndexerRudimentaryImpl()
        return new IndexerByVisitorImpl()
    }

    static Indexer newIndexer(String indexerClassName)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> c
        c = Class.forName(indexerClassName)
        Indexer indexer = (Indexer)c.newInstance()
        return indexer
    }
}
