package com.compdf.client;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author ComPDFKit-WPH 2025/2/26 0026
 */
@FunctionalInterface
public interface StreamCompletionCallback {

    void onComplete(SseEmitter emitter,String result);
}
