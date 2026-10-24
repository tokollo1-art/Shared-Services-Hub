package com.ssh.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

/**
 * ActionDto
 *
 * Purpose: The shape of an agent action, as posted by the AI service.
 *          Received by POST /api/v1/agent/actions.
 *
 * This is the mirror of Python's AgentAction (schemas.py). The Python
 * side uses CamelModel with populate_by_name=True, so it sends camelCase
 * keys. This class uses camelCase field names directly; Jackson maps
 * them without annotations.
 *
 * outputs is an arbitrary JSON object — the per-agent payload. counts is
 * a map of string keys to int values, used by the activity feed to render
 * badges. Both are declared as Jackson types so no custom deserialiser
 * is needed.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionDto {

    private String agent;
    private UUID triggerEventId;
    private String inputsSummary;
    private String decision;
    private String reasoning;
    private JsonNode outputs;
    private Map<String, Integer> counts;
    private String mode;
    private Boolean usedFallback;
    private String modelVersion;
    private Integer latencyMs;

    public ActionDto() {}

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public UUID getTriggerEventId() {
        return triggerEventId;
    }

    public void setTriggerEventId(UUID triggerEventId) {
        this.triggerEventId = triggerEventId;
    }

    public String getInputsSummary() {
        return inputsSummary;
    }

    public void setInputsSummary(String inputsSummary) {
        this.inputsSummary = inputsSummary;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public JsonNode getOutputs() {
        return outputs;
    }

    public void setOutputs(JsonNode outputs) {
        this.outputs = outputs;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Integer> counts) {
        this.counts = counts;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean getUsedFallback() {
        return usedFallback;
    }

    public void setUsedFallback(Boolean usedFallback) {
        this.usedFallback = usedFallback;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }
}
