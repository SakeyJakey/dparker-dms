package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private UserRepository userRepository;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(userRepository);
    }

    @Test
    void testExportUsersCsv() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .enabled(true)
            .createdAt(Instant.now())
            .build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        byte[] csv = exportService.exportUsersCsv();
        String csvStr = new String(csv);

        assertNotNull(csv);
        assertTrue(csvStr.contains("ID,Username,Email"));
        assertTrue(csvStr.contains("testuser"));
        assertTrue(csvStr.contains("test@example.com"));
    }

    @Test
    void testExportUsersCsvEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        byte[] csv = exportService.exportUsersCsv();
        String csvStr = new String(csv);

        assertTrue(csvStr.contains("ID,Username,Email"));
        assertEquals(1, csvStr.split("\n").length); // header only
    }

    @Test
    void testExportUsersCsvWithSpecialCharacters() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("user,with,commas")
            .email("test@example.com")
            .displayName("User \"Quoted\"")
            .enabled(true)
            .createdAt(Instant.now())
            .build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        byte[] csv = exportService.exportUsersCsv();
        String csvStr = new String(csv);

        assertTrue(csvStr.contains("\"user,with,commas\""));
    }

    @Test
    void testExportAuditCsv() {
        byte[] csv = exportService.exportAuditCsv("CREATE", "2026-01-01", "2026-02-14");
        String csvStr = new String(csv);

        assertNotNull(csv);
        assertTrue(csvStr.contains("Timestamp,Event Type"));
        assertTrue(csvStr.contains("CREATE"));
    }

    @Test
    void testExportAuditCsvWithNullFilters() {
        byte[] csv = exportService.exportAuditCsv(null, null, null);
        String csvStr = new String(csv);

        assertTrue(csvStr.contains("ALL"));
        assertTrue(csvStr.contains("N/A"));
    }

    @Test
    void testExportComplianceReport() {
        byte[] report = exportService.exportComplianceReport("csv");
        String reportStr = new String(report);

        assertNotNull(report);
        assertTrue(reportStr.contains("DMS Compliance Report"));
        assertTrue(reportStr.contains("PCI-DSS"));
        assertTrue(reportStr.contains("GDPR"));
        assertTrue(reportStr.contains("ISO 27001"));
    }
}
