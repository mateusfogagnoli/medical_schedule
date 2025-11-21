package br.usuario.clinica;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import br.usuario.clinica.dao.InMemoryDoctorDAO;
import br.usuario.clinica.dao.InMemoryPatientDAO;
import br.usuario.clinica.model.Appointment;
import br.usuario.clinica.model.Doctor;
import br.usuario.clinica.model.Patient;
import br.usuario.clinica.service.Scheduler;
import br.usuario.clinica.util.ExportUtil;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        InMemoryPatientDAO patientDAO = new InMemoryPatientDAO();
        InMemoryDoctorDAO doctorDAO = new InMemoryDoctorDAO();
        Scheduler scheduler = new Scheduler();
        System.out.println("Agendador de Consultas (memória) — dados serão apagados ao sair");
        boolean running = true;
        while (running) {
            showMenu();
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": addPatient(patientDAO); break;
                case "2": addDoctor(doctorDAO); break;
                case "3": scheduleAppointment(patientDAO, doctorDAO, scheduler); break;
                case "4": listAppointments(scheduler, patientDAO, doctorDAO); break;
                case "5": exportAgenda(scheduler); break;
                case "6": cancelAppointment(scheduler); break;
                case "7": listWaiting(scheduler); break;
                case "0": running = false; break;
                default: System.out.println("Opção inválida");
            }
        }
        System.out.println("Encerrando — os dados em memória serão perdidos.");
    }
    private static void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("1) Cadastrar paciente");
        System.out.println("2) Cadastrar médico");
        System.out.println("3) Agendar consulta");
        System.out.println("4) Listar consultas");
        System.out.println("5) Exportar agenda (CSV)");
        System.out.println("6) Cancelar consulta (por id)");
        System.out.println("7) Ver lista de espera");
        System.out.println("0) Sair");
        System.out.print("Escolha: ");
    }
    private static void addPatient(InMemoryPatientDAO dao) {
        System.out.print("Nome do paciente: "); String name = scanner.nextLine().trim();
        System.out.print("Telefone: "); String phone = scanner.nextLine().trim();
        Patient p = dao.addPatient(name, phone); System.out.println("Criado: " + p);
    }
    private static void addDoctor(InMemoryDoctorDAO dao) {
        System.out.print("Nome do médico: "); String name = scanner.nextLine().trim();
        System.out.print("Especialidade: "); String spec = scanner.nextLine().trim();
        Doctor d = dao.addDoctor(name, spec); System.out.println("Criado: " + d);
    }
    private static void scheduleAppointment(InMemoryPatientDAO pDao, InMemoryDoctorDAO dDao, Scheduler scheduler) {
        try {
            System.out.println("Pacientes:"); for (Patient p : pDao.listAll()) System.out.println(p);
            System.out.print("Id do paciente: "); int pid = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("Médicos:"); for (Doctor d : dDao.listAll()) System.out.println(d);
            System.out.print("Id do médico: "); int did = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Início (yyyy-MM-ddTHH:mm): "); String startS = scanner.nextLine().trim();
            System.out.print("Fim (yyyy-MM-ddTHH:mm): "); String endS = scanner.nextLine().trim();
            LocalDateTime start = LocalDateTime.parse(startS); LocalDateTime end = LocalDateTime.parse(endS);
            var appt = scheduler.schedule(pid, did, start, end);
            System.out.println(appt == null ? "Conflito detectado — adicionado à lista de espera." : "Agendado: " + appt);
        } catch (Exception ex) { System.out.println("Erro ao agendar: " + ex.getMessage()); }
    }
    private static void listAppointments(Scheduler scheduler, InMemoryPatientDAO pDao, InMemoryDoctorDAO dDao) {
        List<Appointment> list = scheduler.listAppointments(); DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (list.isEmpty()) { System.out.println("Nenhuma consulta agendada."); return; }
        for (Appointment a : list) { String pName = pDao.findById(a.getPatientId()).getName(); String dName = dDao.findById(a.getDoctorId()).getName(); System.out.printf("id=%d | %s - %s | paciente=%s | médico=%s\n", a.getId(), a.getStart().format(fmt), a.getEnd().format(fmt), pName, dName); }
    }
    private static void exportAgenda(Scheduler scheduler) {
        try { File out = new File("agenda_export.csv"); ExportUtil.exportAppointmentsCsv(scheduler.listAppointments(), out); System.out.println("Agenda exportada para: " + out.getAbsolutePath()); } catch (Exception ex) { System.out.println("Erro ao exportar: " + ex.getMessage()); }
    }
    private static void cancelAppointment(Scheduler scheduler) {
        try { System.out.print("Id da consulta a cancelar: "); int id = Integer.parseInt(scanner.nextLine().trim()); boolean ok = scheduler.cancelAppointment(id); System.out.println(ok ? "Cancelada." : "Não encontrada."); } catch (Exception ex) { System.out.println("Erro: " + ex.getMessage()); }
    }
    private static void listWaiting(Scheduler scheduler) {
        var list = scheduler.listWaiting(); if (list.isEmpty()) { System.out.println("Lista de espera vazia."); return; }
        System.out.println("Lista de espera (FIFO):"); for (var w : list) { System.out.println("patient=" + w.patientId + ", doctor=" + w.doctorId + ", " + w.start + " - " + w.end); }
    }
}
