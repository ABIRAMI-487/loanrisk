package com.example.loanrisk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin wrapper around Google's Gemini API (generateContent endpoint).
 * Used ONLY to turn our rule-based risk output into natural-language text
 * (email wording, plain-English explanations).
 *
 * The risk DECISION itself always stays rule-based in RiskScoringService —
 * Gemini never decides risk level, it only writes the communication text.
 * If Gemini fails or isn't configured, callers fall back to a fixed template.
 */
@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String callGemini(String prompt) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key not configured");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        String escapedPrompt = objectMapper.writeValueAsString(prompt);
        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":" + escapedPrompt + "}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Gemini API returned status " + response.statusCode() + ": " + response.body());
            throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode textNode = root.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini API returned empty response text");
        }
        return textNode.asText().trim();
    }

    /**
     * Generates a short, personalized EMI reminder email body.
     * Throws on failure - caller (EmailService) catches and falls back to a static template.
     */
    public String generateEmiReminderMessage(String customerName, String emiAmount,
                                              String riskReason, String status) throws Exception {
        String prompt = "Write a short, polite email (under 100 words) to a loan customer named "
                + customerName + " about a " + status.toLowerCase().replace("_", " ")
                + " EMI payment of Rs. " + emiAmount + ". "
                + "Context (for tone only, do not quote it directly): " + riskReason + ". "
                + "Keep the tone professional, respectful, and non-threatening — this is a reminder, not a threat. "
                + "Sign off as 'Loan Servicing Team'. Return only the email body text, no subject line, no placeholders.";
        return callGemini(prompt);
    }

    /**
     * Generates a one-line, plain-English explanation of a rule-based risk score,
     * for display on the dashboard. Fallbacks gracefully to smart rule explanation if Gemini API is rate limited.
     */
    public String generateRiskExplanation(String customerName, String riskLevel, String ruleReason) {
        try {
            String prompt = "In one short, plain-English sentence (max 30 words), explain to a bank manager "
                    + "why customer " + customerName + " has been flagged as " + riskLevel
                    + " risk, based on this rule-based finding: \"" + ruleReason + "\". "
                    + "Be factual, do not exaggerate, do not add advice, do not repeat the customer's name unnecessarily.";
            return callGemini(prompt);
        } catch (Exception e) {
            System.out.println("Gemini explanation API unavailable (" + e.getMessage() + "), using fallback explanation.");
            return generateFallbackExplanation(customerName, riskLevel, ruleReason);
        }
    }

    /**
     * Smart, natural-language plain-English explanation fallback when Gemini API is rate-limited or offline.
     */
    public String generateFallbackExplanation(String customerName, String riskLevel, String ruleReason) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return customerName + " presents high default risk because multiple recent EMI payments were late or missed (" + ruleReason + ").";
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return customerName + " has moderate default risk due to an irregular payment in recent EMI history (" + ruleReason + ").";
        } else {
            return customerName + " maintains low default risk with a consistent, on-time EMI payment track record.";
        }
    }
}
