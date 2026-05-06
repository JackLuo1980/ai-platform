package com.aiplatform.inference.common;

import lombok.Data;

@Data
public class PageRequest {

    private int page = 0;
    private int size = 20;
    private String keyword;
}
