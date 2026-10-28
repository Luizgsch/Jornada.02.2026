package br.com.cashmobile.movement;

public class MovementFailure extends RuntimeException {
    public enum Reason { INVALID, NOT_FOUND, CONFLICT, CAPACITY }
    private final Reason reason;
    public MovementFailure(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
