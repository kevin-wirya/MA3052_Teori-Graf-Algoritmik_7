package com.grafapp.algorithm;

/**
 * Deskriptor parameter yang diperlukan oleh algoritma.
 * Digunakan untuk membuat UI input dinamis berdasarkan kebutuhan algoritma.
 */
public class ParameterInfo {

    public enum Type {
        NODE_SELECT,
        INTEGER,
        BOOLEAN
    }

    private final String key;
    private final String label;
    private final Type type;
    private final Object defaultValue;
    private final boolean required;

    public ParameterInfo(String key, String label, Type type, Object defaultValue, boolean required) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public Type getType() { return type; }
    public Object getDefaultValue() { return defaultValue; }
    public boolean isRequired() { return required; }
}
