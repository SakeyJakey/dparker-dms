package com.davidparker.dms.compliance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErasureResponse {
    private int deletedCount;
    private int retainedCount;
}
