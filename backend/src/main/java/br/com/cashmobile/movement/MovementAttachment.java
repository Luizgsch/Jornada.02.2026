package br.com.cashmobile.movement;

import java.time.Instant;

// Binário interno e imutável; controllers expõem somente metadados ou conteúdo.
public record MovementAttachment(String name, String contentType, byte[] content, Instant uploadedAt) {
    public MovementAttachment { content = content.clone(); }
    @Override public byte[] content() { return content.clone(); }
    public int size() { return content.length; }
}
