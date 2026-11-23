package com.proyecto.inventario.inventory;



public class InventoryError extends RuntimeException {
    public enum Kind { INVALID, CONFLICT, MISSING, FORBIDDEN, PROVIDER, UNCONFIGURED }
    public final Kind kind;
    public InventoryError(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }
    public static InventoryError invalid(String message) { return new InventoryError(Kind.INVALID, message); }
    public static InventoryError conflict(String message) { return new InventoryError(Kind.CONFLICT, message); }
    public static InventoryError missing(String message) { return new InventoryError(Kind.MISSING, message); }
}
