package main.search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import main.index.LuceneConsts;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;
    private QueryParser queryParserReleased;
    private QueryParser queryParserTitle;
    private QueryParser queryParserAppears;
    private QueryParser queryParserArtist;
    private QueryParser queryParserWriter;
    private QueryParser queryParserProducer;
    private QueryParser queryParserDescription;
    private Query query;

    public Searcher(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
        queryParser = new QueryParser(LuceneConsts.CONTENTS, new StandardAnalyzer());
        queryParserTitle = new QueryParser(LuceneConsts.title, new StandardAnalyzer());
        queryParserReleased = new QueryParser(LuceneConsts.released, new StandardAnalyzer());
        queryParserAppears = new QueryParser(LuceneConsts.appears, new StandardAnalyzer());
        queryParserArtist = new QueryParser(LuceneConsts.artist, new StandardAnalyzer());
        queryParserWriter = new QueryParser(LuceneConsts.writers, new StandardAnalyzer());
        queryParserProducer = new QueryParser(LuceneConsts.producer, new StandardAnalyzer());
        queryParserDescription = new QueryParser(LuceneConsts.description, new StandardAnalyzer());
    }

    public List<Document> search(String searchQuery, String field) throws IOException, ParseException {
        if (field.equals("")) {
            query = queryParser.parse(searchQuery);
        } else if (field.equals("title")) {
            query = queryParserTitle.parse(searchQuery);
        } else if (field.equals("released")) {
            query = queryParserReleased.parse(searchQuery);
        } else if (field.equals("appears")) {
            query = queryParserAppears.parse(searchQuery);
        } else if (field.equals("artist")) {
            query = queryParserArtist.parse(searchQuery);
        } else if (field.equals("writers")) {
            query = queryParserWriter.parse(searchQuery);
        } else if (field.equals("producer")) {
            query = queryParserProducer.parse(searchQuery);
        } else if (field.equals("description")) {
            query = queryParserDescription.parse(searchQuery);
        }

        ArrayList<Document> resultDocs = new ArrayList<>();

        ScoreDoc[] hits = indexSearcher.search(query, LuceneConsts.MAX_SEARCH).scoreDocs;
        for (ScoreDoc hit : hits) {
            Document doc = indexSearcher.doc(hit.doc);
            resultDocs.add(doc);
        }

        return resultDocs;
    }

}
