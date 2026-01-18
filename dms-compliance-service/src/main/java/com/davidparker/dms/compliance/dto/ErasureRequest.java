package com.davidparker.dms.compliance.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ErasureRequest {
    private UUID dataSubjectId;
}
