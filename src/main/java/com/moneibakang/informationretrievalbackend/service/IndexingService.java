package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class IndexingService {
    private final Directory directory;
    private final Analyzer analyzer;
    private final Tokenizer tokenizer;

    public IndexingService(@Qualifier("simpleTokenizer") Tokenizer tokenizer) throws IOException {
        this.tokenizer = tokenizer;
        Path indexPath = Paths.get("index_" + tokenizer.getName().toLowerCase());
        this.directory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();
    }

    public void indexDocuments(List<Document> documents) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (Document doc : documents) {
                org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
                
                // Add fields
                luceneDoc.add(new StringField("id", doc.getId(), Field.Store.YES));
                luceneDoc.add(new TextField("title", doc.getTitle(), Field.Store.YES));
                luceneDoc.add(new TextField("content", doc.getContent(), Field.Store.YES));
                luceneDoc.add(new StringField("author", doc.getAuthor(), Field.Store.YES));
                luceneDoc.add(new StringField("dataset", doc.getDataset(), Field.Store.YES));
                
                // Add tokenized content for custom ranking
                List<String> tokens = tokenizer.tokenize(doc.getContent());
                luceneDoc.add(new TextField("tokens", String.join(" ", tokens), Field.Store.YES));
                
                writer.addDocument(luceneDoc);
            }
        }
    }

    public void close() throws IOException {
        directory.close();
        analyzer.close();
    }
} 