package main.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Indexer {

    private IndexWriter writer;
    private Analyzer standardAnalyzer;

    private Path indexDirectory;
    private File dpathDirectory;

    public Indexer(String indexDirectoryPath, String dpathDirectoryPath) {
        this.indexDirectory = Paths.get(indexDirectoryPath);
        this.dpathDirectory = new File(dpathDirectoryPath);
        this.standardAnalyzer = new StandardAnalyzer();
    }

    public int createIndexer() throws IOException {
        Directory dir = FSDirectory.open(indexDirectory);
        IndexWriterConfig indexConfig = new IndexWriterConfig(standardAnalyzer);
        writer = new IndexWriter(dir, indexConfig);
        writer.deleteAll();

        File[] txtFiles = dpathDirectory.listFiles();
        ArrayList<File> files = new ArrayList<>();
        for (File f : txtFiles) {
            if (f.getName().toLowerCase().endsWith(".txt")) {
                files.add(f);
            }
        }

        for (File file : files) {
            System.out.println("Indexing: " + file.getCanonicalPath());
            Document doc = new Document();

            Field filePathField = new StoredField(LuceneConsts.FILE_PATH, file.getCanonicalPath());
            List<String> lines = Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.ISO_8859_1);
            String content = String.join(System.lineSeparator(), lines);

            Field contents = new TextField(LuceneConsts.CONTENTS, content, Field.Store.YES);
            Scanner scanner = new Scanner(file, StandardCharsets.ISO_8859_1);
            Field title = new TextField(LuceneConsts.title, scanner.nextLine(), Field.Store.YES);
            Field description = new TextField(LuceneConsts.description, scanner.nextLine(), Field.Store.YES);
            Field appears = new TextField(LuceneConsts.appears, scanner.nextLine(), Field.Store.YES);
            Field artist = new TextField(LuceneConsts.artist, scanner.nextLine(), Field.Store.YES);
            Field writers = new TextField(LuceneConsts.writers, scanner.nextLine(), Field.Store.YES);
            Field producer = new TextField(LuceneConsts.producer, scanner.nextLine(), Field.Store.YES);
            Field released = new TextField(LuceneConsts.released, scanner.nextLine(), Field.Store.YES);

            doc.add(filePathField);
            doc.add(title);
            doc.add(description);
            doc.add(appears);
            doc.add(artist);
            doc.add(writers);
            doc.add(producer);
            doc.add(released);
            doc.add(contents);
            scanner.close();

            writer.addDocument(doc);
        }

        writer.commit();
        int numDocs = writer.getDocStats().numDocs;
        writer.close();
        dir.close();

        return numDocs;
    }
}
