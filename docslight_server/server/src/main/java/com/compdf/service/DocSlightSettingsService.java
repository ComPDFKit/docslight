package com.compdf.service;

import com.compdf.entity.DocSlightSettings;

/**
 * @author ComPDFKit-WPH 2024-09-11
 */
public interface DocSlightSettingsService {

    DocSlightSettings getSettings();

    DocSlightSettings updateSettings(DocSlightSettings docSlightSettings);
}