package com.innoura.legalEase.helper;

import com.innoura.legalEase.enums.FileType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiHelper
{

    public void appendSection(StringBuilder html, String title, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        html.append("<h3><b>").append(title).append("</b></h3>");
        html.append("<ul>");

        for (String item : items) {
            html.append("<li>")
                    .append(escapeHtml(item))
                    .append("</li>");
        }

        html.append("</ul>");
    }
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public String getEvidenceType(FileType fileType)
    {
        return switch (fileType) {
            case PDF -> "PDF EVIDENCE";
            case EXCEL -> "EXCEL EVIDENCE";
            case AUDIO -> "AUDIO EVIDENCE";
            case IMAGE -> "IMAGE EVIDENCE";
            default -> "";
        };
    }


}
