package com.example.vibe_store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import com.example.vibe_store.config.ToolsConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final ChatClient chatClient;
    private VectorStore vectorStore;
    private final ToolsConfig toolsConfig;
    private final EmbeddingModel embeddingModel;

    public void addDocuments(List<String> texts) {
        File vectorFile = new File("ai-data", "vector-store.json");
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        if (vectorFile.exists()) {
            vectorFile.delete();
            log.info("Köhnə vektor faylı silindi: {}", vectorFile.getAbsolutePath());
        }

        int CHUNK_SIZE = 800;
        int OVERLAP = 200;

        List<Document> documents = texts.stream()
                .map(text -> splitTextIntoChunks(text, CHUNK_SIZE, OVERLAP))
                .flatMap(List::stream)
                .map(Document::new)
                .toList();

        vectorStore.add(documents);
        saveVectorStore();
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAskWithRag")
    @Retry(name = "aiService")
    @RateLimiter(name = "aiService")
    public Map<String, String> askWithRag(String question) {

        if (vectorStore == null) {
            return Map.of("answer", "Əvvəlcə sənəd əlavə edin.", "context", "");
        }

        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build()
        );

        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        if (context.isBlank()) {
            return Map.of(
                "answer", "Bilgi bazasında bu suala aid məlumat tapılmadı.",
                "context", ""
            );
        }

        String answer = chatClient.prompt()
                .user("Kontekst:\n" + context + "\n\nSual: " + question)
                .call()
                .content();

        return Map.of(
            "answer", answer,
            "context", context
        );
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAskSimple")
    @Retry(name = "aiService")
    @RateLimiter(name = "aiService")
    public String askSimple(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAskWithTools")
    @Retry(name = "aiService")
    @RateLimiter(name = "aiService")
    public String askWithTools(String question) {
        return chatClient.prompt()
                .user(question)
                .tools(toolsConfig)
                .call()
                .content();
    }

    public Map<String, String> fallbackAskWithRag(String question, Throwable t) {
        log.error("Fallback askWithRag triggered. Error: {}", t.getMessage());
        return Map.of(
                "answer", "AI service is currently unavailable. Please try again later.",
                "context", "System error: " + t.getClass().getSimpleName()
        );
    }

    public String fallbackAskSimple(String question, Throwable t) {
        log.error("Fallback askSimple triggered. Error: {}", t.getMessage());
        return "AI service is currently unavailable. Please try again later.";
    }

    public String fallbackAskWithTools(String question, Throwable t) {
        log.error("Fallback askWithTools triggered. Error: {}", t.getMessage());
        return "AI service is currently unavailable. Please try again later.";
    }

    private void saveVectorStore() {
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            File vectorFile = new File("ai-data", "vector-store.json");
            simpleStore.save(vectorFile);
            log.info("Vektor bazası faylda saxlandı: {}", vectorFile.getAbsolutePath());
        }
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = Math.min(startIndex + chunkSize, text.length());
            chunks.add(text.substring(startIndex, endIndex));
            if (endIndex == text.length()) {
                break;
            }
            startIndex = endIndex - overlapSize;
        }
        return chunks;
    }
}