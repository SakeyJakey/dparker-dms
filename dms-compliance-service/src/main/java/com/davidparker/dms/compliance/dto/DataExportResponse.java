package com.davidparker.dms.compliance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@Data
@AllArgsConstructor
public class DataExportResponse {
    private String exportPath;
    private Duration expiration;
}
