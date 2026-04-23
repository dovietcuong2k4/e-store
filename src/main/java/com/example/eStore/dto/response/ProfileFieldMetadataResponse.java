package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileFieldMetadataResponse {
    private String name;
    private String label;
    private String type;
    private boolean editable;
    private boolean required;
    private Integer maxLength;
    private String pattern;
    private String placeholder;
    private Integer order;
}
