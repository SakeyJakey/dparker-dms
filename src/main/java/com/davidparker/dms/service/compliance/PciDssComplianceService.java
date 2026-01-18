package com.davidparker.dms.service.compliance;

import com.davidparker.dms.model.Document;
import com.davidparker.dms.service.PermissionService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Component
public class PciDssComplianceService {

    private final PermissionService permissionService;
    private static final Pattern PAN_PATTERN = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3[0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\b[0-9]{3,4}\\b");

    public PciDssComplianceService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Service
    public static class CardholderDataProtection {
        
        private final PciDssComplianceService parent;

        public CardholderDataProtection(PciDssComplianceService parent) {
            this.parent = parent;
        }

        public void classifyDocument(Document document, String content) {
            if (containsCardholderData(content)) {
                document.setClassification(Document.Classification.PCI);
                document.setPciRelevant(true);
                
                // Apply additional restrictions would be handled by PermissionService
            }
        }
        
        private boolean containsCardholderData(String content) {
            if (content == null) {
                return false;
            }
            return PAN_PATTERN.matcher(content).find() || CVV_PATTERN.matcher(content).find();
        }
    }
}
