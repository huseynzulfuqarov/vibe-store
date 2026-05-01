package com.example.vibe_store.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    Sən Vibe Store mağazalar şəbəkəsinin köməkçi assistentisən.
                    Yalnız verilən kontekstə əsasən cavab ver.
                    Kontekstdə cavab yoxdursa, "Bu barədə məlumatım yoxdur" de.
                    Cavabları qısa və dəqiq ver. Azərbaycan dilində cavab ver.
                    """)
                .build();
    }

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        File dataDir = new File("ai-data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File vectorFile = new File(dataDir, "vector-store.json");
        if (vectorFile.exists() && vectorFile.length() > 0) {
            try {
                store.load(vectorFile);
                log.info("Vektor bazası fayldan yükləndi: {}", vectorFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("Vektor faylı oxuna bilmədi (ola bilsin zədələnib və ya yarımçıq qalıb): {}", e.getMessage());
                log.info("Korlanmış fayl silinir, yeni baza yaradılacaq...");
                vectorFile.delete();
            }
        } else {
            log.info("Vektor bazası boşdur (və ya yoxdur). /api/ai/documents ilə sənəd əlavə edin.");
        }

        return store;
    }
}
