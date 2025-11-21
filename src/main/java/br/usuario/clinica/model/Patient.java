package br.usuario.clinica.model;

public class Patient {
    private final int id;
    private final String name;
    private final String phone;
    public Patient(int id, String name, String phone) { this.id = id; this.name = name; this.phone = phone; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    @Override public String toString() { return id + ": " + name + " (" + phone + ")"; }
}
