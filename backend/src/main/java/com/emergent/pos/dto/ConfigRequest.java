package com.emergent.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Body of POST /api/config — matches the FastAPI backend's AppConfigCreate
// schema field names exactly (snake_case, as sent by every client's
// apiClient.setConfig()/ApiClient.setConfig()).
@Getter
@Setter
public class ConfigRequest {

    @NotBlank
    @JsonProperty("config_key")
    private String configKey;

    @JsonProperty("config_value")
    private String configValue;
}
