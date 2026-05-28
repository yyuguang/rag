package com.lnzz.rag.common.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class CommonModuleStructureTest {

    @Test
    void parentPomDeclaresOnlyCommonAndConfigModules() throws Exception {
        Path root = ragAgentRoot();
        Document pom = readPom(root.resolve("pom.xml"));
        Element project = pom.getDocumentElement();

        assertEquals("pom", directChildText(project, "packaging"));
        assertEquals(List.of("common", "config"), moduleNames(project));
        assertEquals("17", propertyValue(project, "java.version"));
        assertTrue(propertyValue(project, "spring-boot.version").startsWith("3."));
        assertFalse(propertyValue(project, "maven-compiler-plugin.version").isBlank());
        assertFalse(propertyValue(project, "maven-surefire-plugin.version").isBlank());
        assertFalse(propertyValue(project, "spring-boot-maven-plugin.version").isBlank());
    }

    @Test
    void commonModuleUsesExpectedParentAndPackageRoot() throws Exception {
        Path root = ragAgentRoot();
        Document pom = readPom(root.resolve("common/pom.xml"));
        Element project = pom.getDocumentElement();
        Element parent = firstDirectChild(project, "parent");

        assertEquals("rag-agent", directChildText(parent, "artifactId"));
        assertEquals("common", directChildText(project, "artifactId"));
        assertTrue(Files.isDirectory(root.resolve("common/src/main/java/com/lnzz/rag/common")));
    }

    @Test
    void task001DoesNotCreateApplicationOrMicroserviceModules() {
        Path root = ragAgentRoot();
        List<String> forbiddenModules = List.of(
                "rag-agent-app",
                "domain",
                "infrastructure",
                "services",
                "auth-service",
                "kb-service",
                "document-service",
                "document-worker",
                "embedding-service",
                "retrieval-service",
                "rag-chat-service",
                "llm-gateway-service",
                "prompt-service",
                "feedback-service",
                "evaluation-service",
                "audit-service",
                "statistics-service",
                "admin-config-service");

        List<String> existingForbiddenModules = forbiddenModules.stream()
                .filter(module -> Files.exists(root.resolve(module)))
                .toList();

        assertTrue(existingForbiddenModules.isEmpty(),
                "Forbidden TASK-001 modules should not exist: " + existingForbiddenModules);
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

    private static List<String> moduleNames(Element project) {
        Element modules = firstDirectChild(project, "modules");
        List<String> names = new ArrayList<>();
        NodeList children = modules.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && "module".equals(element.getTagName())) {
                names.add(element.getTextContent().trim());
            }
        }
        return names;
    }

    private static String propertyValue(Element project, String propertyName) {
        Element properties = firstDirectChild(project, "properties");
        return directChildText(properties, propertyName);
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
