package com.lnzz.rag.config.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ConfigModuleStructureTest {

    @Test
    void configModuleUsesExpectedParentAndPackageRoot() throws Exception {
        Path root = ragAgentRoot();
        Document pom = readPom(root.resolve("config/pom.xml"));
        Element project = pom.getDocumentElement();
        Element parent = firstDirectChild(project, "parent");

        assertEquals("rag-agent", directChildText(parent, "artifactId"));
        assertEquals("config", directChildText(project, "artifactId"));
        assertTrue(Files.isDirectory(root.resolve("config/src/main/java/com/lnzz/rag/config")));
    }

    @Test
    void configModuleDoesNotWireRealInfrastructureInTask001() {
        Path root = ragAgentRoot();
        List<Path> forbiddenPaths = List.of(
                root.resolve("config/src/main/resources/application.yml"),
                root.resolve("config/src/main/resources/application.yaml"),
                root.resolve("config/src/main/resources/application.properties"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/db"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/redis"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/mq"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/minio"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/llm"),
                root.resolve("config/src/main/java/com/lnzz/rag/config/rag"));

        List<Path> existingForbiddenPaths = forbiddenPaths.stream()
                .filter(Files::exists)
                .toList();

        assertTrue(existingForbiddenPaths.isEmpty(),
                "TASK-001 must not wire real infrastructure: " + existingForbiddenPaths);
    }

    private static Path ragAgentRoot() {
        Path current = Paths.get("").toAbsolutePath();
        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("common"))
                    && Files.isDirectory(candidate.resolve("config"))) {
                return candidate;
            }
            if (candidate.getFileName() != null && "rag-agent".equals(candidate.getFileName().toString())) {
                return candidate;
            }
        }
        throw new AssertionError("Cannot locate rag-agent root from " + current);
    }

    private static Document readPom(Path pomPath) throws Exception {
        assertTrue(Files.isRegularFile(pomPath), "Missing POM: " + pomPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(pomPath.toFile());
    }

    private static Element firstDirectChild(Element parent, String childName) {
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && childName.equals(element.getTagName())) {
                return element;
            }
        }
        throw new AssertionError("Missing child <" + childName + "> under <" + parent.getTagName() + ">");
    }

    private static String directChildText(Element parent, String childName) {
        return firstDirectChild(parent, childName).getTextContent().trim();
    }
}
