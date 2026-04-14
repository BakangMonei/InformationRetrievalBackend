package com.moneibakang.informationretrievalbackend.util;

import com.moneibakang.informationretrievalbackend.model.Document;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PubMedCorpusReader {

    public List<Document> readCorpus(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return readCorpus(in, path.toString().toLowerCase(Locale.ROOT));
        }
    }

    public List<Document> readCorpus(InputStream inputStream, String nameHint) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(inputStream, 64 * 1024);
        byte[] probe = new byte[8192];
        int n = pb.read(probe);
        if (n <= 0) {
            return List.of();
        }
        pb.unread(probe, 0, n);
        String head = new String(probe, 0, n, StandardCharsets.UTF_8).trim();
        boolean looksXml = head.startsWith("<?xml") || head.startsWith("<") && head.contains("PubmedArticle");
        if (looksXml || (nameHint != null && nameHint.endsWith(".xml"))) {
            return parsePubmedXml(pb);
        }
        return parseMedline(new BufferedReader(new InputStreamReader(pb, StandardCharsets.UTF_8)));
    }

    private List<Document> parseMedline(BufferedReader reader) throws IOException {
        List<Document> documents = new ArrayList<>();
        String line;
        String currentId = null;
        StringBuilder title = new StringBuilder();
        StringBuilder author = new StringBuilder();
        StringBuilder body = new StringBuilder();
        FieldKind last = FieldKind.NONE;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("PMID- ")) {
                addMedlineDoc(documents, currentId, title, author, body);
                currentId = line.substring(6).trim();
                title.setLength(0);
                author.setLength(0);
                body.setLength(0);
                last = FieldKind.NONE;
            } else if (line.startsWith("TI  - ")) {
                title.append(line.substring(6).trim());
                last = FieldKind.TITLE;
            } else if (line.startsWith("AU  - ")) {
                if (author.length() > 0) {
                    author.append("; ");
                }
                author.append(line.substring(6).trim());
                last = FieldKind.AUTHOR;
            } else if (line.startsWith("AB  - ")) {
                body.append(line.substring(6).trim());
                last = FieldKind.ABSTRACT;
            } else if (!line.isEmpty() && line.charAt(0) == ' ' && last != FieldKind.NONE) {
                String cont = line.trim();
                switch (last) {
                    case TITLE -> title.append(' ').append(cont);
                    case AUTHOR -> author.append(' ').append(cont);
                    case ABSTRACT -> body.append(' ').append(cont);
                    default -> {
                    }
                }
            } else if (line.isEmpty()) {
                last = FieldKind.NONE;
            }
        }
        addMedlineDoc(documents, currentId, title, author, body);
        return documents;
    }

    private static void addMedlineDoc(
            List<Document> documents,
            String currentId,
            StringBuilder title,
            StringBuilder author,
            StringBuilder body) {
        if (currentId == null) {
            return;
        }
        Document doc = new Document();
        doc.setId("PUBMED-" + currentId);
        doc.setTitle(nonBlank(title.toString(), "(no title)"));
        doc.setAuthor(author.toString().trim());
        doc.setContent(nonBlank(body.toString(), "(no content)"));
        doc.setDataset("PUBMED");
        doc.setCollection("PUBMED");
        doc.setTimestamp(System.currentTimeMillis());
        documents.add(doc);
    }

    private List<Document> parsePubmedXml(InputStream in) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            org.w3c.dom.Document xml = factory.newDocumentBuilder().parse(in);
            var xp = XPathFactory.newInstance().newXPath();
            NodeList articles = (NodeList) xp.evaluate(
                    "//*[local-name()='PubmedArticle']",
                    xml.getDocumentElement(),
                    XPathConstants.NODESET);
            List<Document> out = new ArrayList<>();
            for (int i = 0; i < articles.getLength(); i++) {
                Node n = articles.item(i);
                if (!(n instanceof Element el)) {
                    continue;
                }
                String pmid = firstTextByLocalName(el, xp, "PMID");
                if (pmid.isEmpty()) {
                    continue;
                }
                String title = firstTextByLocalName(el, xp, "ArticleTitle");
                StringBuilder abs = new StringBuilder();
                NodeList abstractTexts = (NodeList) xp.evaluate(
                        ".//*[local-name()='AbstractText']",
                        el,
                        XPathConstants.NODESET);
                for (int j = 0; j < abstractTexts.getLength(); j++) {
                    Node at = abstractTexts.item(j);
                    if (at != null && at.getTextContent() != null) {
                        if (abs.length() > 0) {
                            abs.append('\n');
                        }
                        abs.append(at.getTextContent().trim());
                    }
                }
                if (abs.isEmpty()) {
                    Node absNode = (Node) xp.evaluate(
                            ".//*[local-name()='Abstract'][1]",
                            el,
                            XPathConstants.NODE);
                    if (absNode != null && absNode.getTextContent() != null) {
                        abs.append(absNode.getTextContent().trim());
                    }
                }
                Document doc = new Document();
                doc.setId("PUBMED-" + pmid);
                doc.setTitle(nonBlank(title, "(no title)"));
                doc.setAuthor("");
                doc.setContent(nonBlank(abs.toString(), "(no content)"));
                doc.setDataset("PUBMED");
                doc.setCollection("PUBMED");
                doc.setTimestamp(System.currentTimeMillis());
                out.add(doc);
            }
            if (out.isEmpty()) {
                // Fallback for topic-style XML files (e.g. TREC PM topics) so imports don't fail.
                out = parseTopicStyleXml(xml, xp);
            }
            return out;
        } catch (Exception e) {
            throw new IOException("Failed to parse PubMed XML: " + e.getMessage(), e);
        }
    }

    private List<Document> parseTopicStyleXml(org.w3c.dom.Document xml, javax.xml.xpath.XPath xp) throws Exception {
        NodeList topics = (NodeList) xp.evaluate(
                "//*[local-name()='topic']",
                xml.getDocumentElement(),
                XPathConstants.NODESET);
        List<Document> out = new ArrayList<>();
        for (int i = 0; i < topics.getLength(); i++) {
            Node n = topics.item(i);
            if (!(n instanceof Element el)) {
                continue;
            }
            String number = el.getAttribute("number");
            if (number == null || number.isBlank()) {
                number = Integer.toString(i + 1);
            }
            String disease = firstTextByLocalName(el, xp, "disease");
            String gene = firstTextByLocalName(el, xp, "gene");
            String demographic = firstTextByLocalName(el, xp, "demographic");

            String title = nonBlank(disease, "Topic " + number);
            StringBuilder content = new StringBuilder();
            if (!disease.isBlank()) {
                content.append("Disease: ").append(disease);
            }
            if (!gene.isBlank()) {
                if (content.length() > 0) {
                    content.append('\n');
                }
                content.append("Gene: ").append(gene);
            }
            if (!demographic.isBlank()) {
                if (content.length() > 0) {
                    content.append('\n');
                }
                content.append("Demographic: ").append(demographic);
            }

            Document doc = new Document();
            doc.setId("PUBMED-TOPIC-" + number);
            doc.setTitle(title);
            doc.setAuthor("");
            doc.setContent(nonBlank(content.toString(), "(no content)"));
            doc.setDataset("PUBMED");
            doc.setCollection("PUBMED");
            doc.setTimestamp(System.currentTimeMillis());
            out.add(doc);
        }
        return out;
    }

    private static String firstTextByLocalName(Element context, javax.xml.xpath.XPath xp, String localName)
            throws Exception {
        Node n = (Node) xp.evaluate(
                ".//*[local-name()='" + localName + "'][1]",
                context,
                XPathConstants.NODE);
        if (n != null && n.getTextContent() != null) {
            return n.getTextContent().trim();
        }
        return "";
    }

    private static String nonBlank(String value, String fallback) {
        String v = value == null ? "" : value.trim();
        return v.isEmpty() ? fallback : v;
    }

    private enum FieldKind {
        NONE, TITLE, AUTHOR, ABSTRACT
    }
}
