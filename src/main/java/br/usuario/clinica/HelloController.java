package br.usuario.clinica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import br.usuario.clinica.dao.JdbcDoctorDAO;
import br.usuario.clinica.dao.JdbcPatientDAO;
import br.usuario.clinica.model.Appointment;
import br.usuario.clinica.model.Doctor;
import br.usuario.clinica.model.Patient;
import br.usuario.clinica.service.Scheduler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;

public class HelloController {
    @FXML private TextArea output;
    @FXML private FlowPane toolbar;

    private final JdbcPatientDAO patientDAO = new JdbcPatientDAO();
    private final JdbcDoctorDAO doctorDAO = new JdbcDoctorDAO();
    private final Scheduler scheduler = new Scheduler(new br.usuario.clinica.dao.JdbcAppointmentDAO());
    private final Map<String, Queue<WaitingEntry>> specialtyWaiting = new HashMap<>();
    private boolean darkMode = false;

    @FXML
    public void initialize() {
        output.setText("Pronto.\n");
    }

    @FXML
    private void onToggleTheme() {
        if (output.getScene() == null) return;
        var scene = output.getScene();
        String light = getClass().getResource("/br/usuario/clinica/styles.css").toExternalForm();
        String dark = getClass().getResource("/br/usuario/clinica/styles-dark.css").toExternalForm();
        if (!darkMode) {
            scene.getStylesheets().remove(light);
            if (!scene.getStylesheets().contains(dark)) scene.getStylesheets().add(dark);
            darkMode = true;
            output.appendText("Tema escuro ativado.\n");
        } else {
            scene.getStylesheets().remove(dark);
            if (!scene.getStylesheets().contains(light)) scene.getStylesheets().add(light);
            darkMode = false;
            output.appendText("Tema claro ativado.\n");
        }
    }

    @FXML
    private void onAddPatient() {
        TextInputDialog nameDlg = new TextInputDialog();
        nameDlg.setTitle("Cadastrar paciente");
        nameDlg.setHeaderText("Nome do paciente:");
        Optional<String> nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty()) { output.appendText("Cadastro cancelado.\n"); return; }
        TextInputDialog phoneDlg = new TextInputDialog();
        phoneDlg.setTitle("Cadastrar paciente");
        phoneDlg.setHeaderText("Telefone:");
        Optional<String> phoneOpt = phoneDlg.showAndWait();
        if (phoneOpt.isEmpty()) { output.appendText("Cadastro cancelado.\n"); return; }
        Patient p = patientDAO.addPatient(nameOpt.get().trim(), phoneOpt.get().trim());
        output.appendText("Paciente criado: " + p.getName() + " (" + p.getPhone() + ")\n");
    }

    @FXML
    private void onAddDoctor() {
        TextInputDialog nameDlg = new TextInputDialog();
        nameDlg.setTitle("Cadastrar médico");
        nameDlg.setHeaderText("Nome do médico:");
        Optional<String> nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty()) { output.appendText("Cadastro cancelado.\n"); return; }

        TextInputDialog specDlg = new TextInputDialog();
        specDlg.setTitle("Cadastrar médico");
        specDlg.setHeaderText("Especialidade:");
        Optional<String> specOpt = specDlg.showAndWait();
        if (specOpt.isEmpty()) { output.appendText("Cadastro cancelado.\n"); return; }

        Doctor d = doctorDAO.addDoctor(nameOpt.get().trim(), specOpt.get().trim());
        output.appendText("Médico criado: " + d.getName() + " (" + d.getSpecialty() + ")\n");

        // tentar agendar pacientes em espera por especialidade
        String specKey = d.getSpecialty().toLowerCase();
        Queue<WaitingEntry> q = specialtyWaiting.get(specKey);
        if (q != null && !q.isEmpty()) {
            while (!q.isEmpty()) {
                WaitingEntry entry = q.peek();
                Appointment appt = scheduler.schedule(entry.patientId, d.getId(), entry.start, entry.end);
                if (appt != null) {
                    q.poll();
                    Patient pat = patientDAO.findById(entry.patientId);
                    output.appendText(formatAppointmentFriendly(appt, pat, d) + "\n");
                } else {
                    break; // médico ocupado nesse horário, parar
                }
            }
        }
    }

    @FXML
    private void onEditPatient() {
        List<Patient> list = patientDAO.listAll();
        if (list.isEmpty()) { output.appendText("Nenhum paciente cadastrado.\n"); return; }

        List<String> options = new java.util.ArrayList<>();
        Map<String,Integer> map = new HashMap<>();
        for (Patient p : list) {
            String label = p.getName() + " (" + p.getPhone() + ")";
            options.add(label);
            map.put(label, p.getId());
        }
        ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
        choose.setTitle("Editar paciente");
        choose.setHeaderText("Escolha o paciente para editar:");
        Optional<String> chosen = choose.showAndWait();
        if (chosen.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }
        int id = map.get(chosen.get());
        Patient p = patientDAO.findById(id);

        TextInputDialog nameDlg = new TextInputDialog(p.getName());
        nameDlg.setTitle("Editar paciente");
        nameDlg.setHeaderText("Nome:");
        Optional<String> nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }

        TextInputDialog phoneDlg = new TextInputDialog(p.getPhone());
        phoneDlg.setTitle("Editar paciente");
        phoneDlg.setHeaderText("Telefone:");
        Optional<String> phoneOpt = phoneDlg.showAndWait();
        if (phoneOpt.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }

        Patient updated = patientDAO.updatePatient(id, nameOpt.get().trim(), phoneOpt.get().trim());
        output.appendText("Paciente atualizado: " + updated.getName() + " (" + updated.getPhone() + ")\n");
    }

    @FXML
    private void onEditDoctor() {
        List<Doctor> list = doctorDAO.listAll();
        if (list.isEmpty()) { output.appendText("Nenhum médico cadastrado.\n"); return; }

        List<String> options = new java.util.ArrayList<>();
        Map<String,Integer> map = new HashMap<>();
        for (Doctor d : list) {
            String label = d.getName() + " (" + d.getSpecialty() + ")";
            options.add(label);
            map.put(label, d.getId());
        }
        ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
        choose.setTitle("Editar médico");
        choose.setHeaderText("Escolha o médico para editar:");
        Optional<String> chosen = choose.showAndWait();
        if (chosen.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }
        int id = map.get(chosen.get());
        Doctor d = doctorDAO.findById(id);

        TextInputDialog nameDlg = new TextInputDialog(d.getName());
        nameDlg.setTitle("Editar médico");
        nameDlg.setHeaderText("Nome:");
        Optional<String> nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }

        TextInputDialog specDlg = new TextInputDialog(d.getSpecialty());
        specDlg.setTitle("Editar médico");
        specDlg.setHeaderText("Especialidade:");
        Optional<String> specOpt = specDlg.showAndWait();
        if (specOpt.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }

        Doctor updated = doctorDAO.updateDoctor(id, nameOpt.get().trim(), specOpt.get().trim());
        output.appendText("Médico atualizado: " + updated.getName() + " (" + updated.getSpecialty() + ")\n");

        // alocar pacientes em espera para nova especialidade
        String specKey = updated.getSpecialty().toLowerCase();
        Queue<WaitingEntry> q = specialtyWaiting.get(specKey);
        if (q != null && !q.isEmpty()) {
            while (!q.isEmpty()) {
                WaitingEntry entry = q.peek();
                Appointment appt = scheduler.schedule(entry.patientId, updated.getId(), entry.start, entry.end);
                if (appt != null) {
                    q.poll();
                    Patient pat = patientDAO.findById(entry.patientId);
                    output.appendText(formatAppointmentFriendly(appt, pat, updated) + "\n");
                } else {
                    break;
                }
            }
        }
    }

    @FXML
    private void onEditAppointment() {
        List<Appointment> list = scheduler.listAppointments();
        if (list.isEmpty()) { output.appendText("Nenhuma consulta para editar.\n"); return; }

        List<String> options = new java.util.ArrayList<>();
        for (Appointment a : list) {
            Patient p = patientDAO.findById(a.getPatientId());
            Doctor d = doctorDAO.findById(a.getDoctorId());
            options.add(formatAppointmentFriendly(a, p, d));
        }
        ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
        choose.setTitle("Editar consulta");
        choose.setHeaderText("Escolha a consulta para editar:");
        Optional<String> chosen = choose.showAndWait();
        if (chosen.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }
        int idx = options.indexOf(chosen.get());
        Appointment old = list.get(idx);

        TextInputDialog dateDlg = new TextInputDialog(old.getStart().getDayOfMonth() + "-" + old.getStart().getMonthValue());
        dateDlg.setTitle("Editar consulta");
        dateDlg.setHeaderText("Data (dd-MM):");
        Optional<String> dateOpt = dateDlg.showAndWait();
        if (dateOpt.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }
        String[] parts = dateOpt.get().trim().split("-");
        if (parts.length != 2) { output.appendText("Formato de data inválido. Edição cancelada.\n"); return; }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Year.now().getValue();
        LocalDate date = LocalDate.of(year, month, day);

        LocalTime startTime = promptForTime("Hora de início (HH:mm):");
        if (startTime == null) { output.appendText("Edição cancelada.\n"); return; }
        LocalTime endTime = promptForTime("Hora de término (HH:mm):");
        if (endTime == null) { output.appendText("Edição cancelada.\n"); return; }
        if (!endTime.isAfter(startTime)) { output.appendText("Hora fim deve ser depois da hora início. Edição cancelada.\n"); return; }

        LocalDateTime newStart = LocalDateTime.of(date, startTime);
        LocalDateTime newEnd = LocalDateTime.of(date, endTime);

        List<Doctor> docs = doctorDAO.listAll();
        if (docs.isEmpty()) { output.appendText("Nenhum médico cadastrado.\n"); return; }
        List<String> docOptions = new java.util.ArrayList<>();
        Map<String,Integer> docMap = new HashMap<>();
        for (Doctor d : docs) {
            String label = d.getName() + " (" + d.getSpecialty() + ")";
            docOptions.add(label);
            docMap.put(label, d.getId());
        }
        ChoiceDialog<String> docChoose = new ChoiceDialog<>(docOptions.get(0), docOptions);
        docChoose.setTitle("Escolha médico");
        docChoose.setHeaderText("Escolha médico para esta consulta:");
        Optional<String> docSel = docChoose.showAndWait();
        if (docSel.isEmpty()) { output.appendText("Edição cancelada.\n"); return; }
        int newDocId = docMap.get(docSel.get());
        Appointment candidate = new Appointment(old.getId(), old.getPatientId(), newDocId, newStart, newEnd);
        if (!scheduler.canScheduleIgnoring(old.getId(), candidate)) { output.appendText("Conflito detectado. Edição não realizada.\n"); return; }
        boolean ok = scheduler.updateAppointment(candidate);
        if (ok) {
            Patient p = patientDAO.findById(candidate.getPatientId());
            Doctor d = doctorDAO.findById(candidate.getDoctorId());
            output.appendText("Consulta atualizada: " + formatAppointmentFriendly(candidate, p, d) + "\n");
        } else {
            output.appendText("Falha ao atualizar consulta.\n");
        }
    }

    @FXML
    private void onSchedule() {
        try {
            TextInputDialog patientNameDlg = new TextInputDialog();
            patientNameDlg.setTitle("Agendar consulta");
            patientNameDlg.setHeaderText("Nome do paciente:");
            Optional<String> patientNameOpt = patientNameDlg.showAndWait();
            if (patientNameOpt.isEmpty()) { output.appendText("Agendamento cancelado.\n"); return; }
            String patientName = patientNameOpt.get().trim();

            List<Patient> matches = new java.util.ArrayList<>();
            for (Patient p : patientDAO.listAll()) if (p.getName().equalsIgnoreCase(patientName)) matches.add(p);

            Patient patient;
            if (matches.isEmpty()) {
                TextInputDialog phoneDlg = new TextInputDialog();
                phoneDlg.setTitle("Paciente não encontrado");
                phoneDlg.setHeaderText("Paciente não existe. Informe telefone para criar novo paciente (ou deixe vazio para cancelar):");
                Optional<String> phoneOpt = phoneDlg.showAndWait();
                if (phoneOpt.isEmpty() || phoneOpt.get().trim().isEmpty()) { output.appendText("Agendamento cancelado.\n"); return; }
                patient = patientDAO.addPatient(patientName, phoneOpt.get().trim());
                output.appendText("Paciente criado: " + patient.getName() + " (" + patient.getPhone() + ")\n");
            } else if (matches.size() == 1) {
                patient = matches.get(0);
            } else {
                List<String> options = new java.util.ArrayList<>();
                for (Patient p : matches) options.add(p.getId() + ": " + p.getName() + " (" + p.getPhone() + ")");
                ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
                choose.setTitle("Escolha paciente");
                choose.setHeaderText("Múltiplos pacientes encontrados; escolha um:");
                Optional<String> chosen = choose.showAndWait();
                if (chosen.isEmpty()) { output.appendText("Agendamento cancelado.\n"); return; }
                String sel = chosen.get();
                int selId = Integer.parseInt(sel.split(":")[0]);
                patient = patientDAO.findById(selId);
            }

            TextInputDialog specDlg = new TextInputDialog();
            specDlg.setTitle("Agendar consulta");
            specDlg.setHeaderText("Especialidade desejada (ex.: neuro):");
            Optional<String> specOpt = specDlg.showAndWait();
            if (specOpt.isEmpty() || specOpt.get().trim().isEmpty()) { output.appendText("Agendamento cancelado.\n"); return; }
            String wantedSpec = specOpt.get().trim().toLowerCase();

            List<Doctor> candidates = new java.util.ArrayList<>();
            for (Doctor d : doctorDAO.listAll()) if (d.getSpecialty().toLowerCase().contains(wantedSpec)) candidates.add(d);

            TextInputDialog dateDlg = new TextInputDialog();
            dateDlg.setTitle("Agendar consulta");
            dateDlg.setHeaderText("Data (dia-mês):");
            Optional<String> dateOpt = dateDlg.showAndWait();
            if (dateOpt.isEmpty()) { output.appendText("Agendamento cancelado.\n"); return; }
            String[] parts = dateOpt.get().trim().split("-");
            if (parts.length != 2) { output.appendText("Formato de data inválido. Use dia-mês.\n"); return; }
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Year.now().getValue();
            LocalDate date = LocalDate.of(year, month, day);

            LocalTime startTime = promptForTime("Hora de início (HH:mm, 24h):");
            if (startTime == null) { output.appendText("Agendamento cancelado.\n"); return; }
            LocalTime endTime = promptForTime("Hora de término (HH:mm, 24h):");
            if (endTime == null) { output.appendText("Agendamento cancelado.\n"); return; }
            if (!endTime.isAfter(startTime)) { output.appendText("Hora fim deve ser depois da hora início.\n"); return; }

            LocalDateTime start = LocalDateTime.of(date, startTime);
            LocalDateTime end = LocalDateTime.of(date, endTime);

            if (candidates.isEmpty()) {
                String key = wantedSpec.toLowerCase();
                specialtyWaiting.computeIfAbsent(key, k -> new LinkedList<>())
                        .add(new WaitingEntry(patient.getId(), start, end, wantedSpec));
                output.appendText("Nenhum especialista da área encontrado. Paciente adicionado à lista de espera para " + wantedSpec + ".\n");
                return;
            }

            Appointment scheduled = null;
            Doctor selected = null;
            for (Doctor d : candidates) {
                boolean conflict = false;
                for (Appointment a : scheduler.listAppointments()) {
                    if (a.getDoctorId() == d.getId()) {
                        Appointment cand = new Appointment(-1, patient.getId(), d.getId(), start, end);
                        if (a.overlaps(cand)) { conflict = true; break; }
                    }
                }
                if (!conflict) {
                    scheduled = scheduler.schedule(patient.getId(), d.getId(), start, end);
                    selected = d;
                    break;
                }
            }

            if (scheduled != null) {
                output.appendText(formatAppointmentFriendly(scheduled, patient, selected) + "\n");
            } else {
                output.appendText("Nenhum especialista disponível nesse horário. Deseja entrar na lista de espera?\n");
                ChoiceDialog<String> conf = new ChoiceDialog<>("Sim", List.of("Sim", "Não"));
                conf.setTitle("Lista de espera");
                conf.setHeaderText("Adicionar à lista de espera?");
                Optional<String> confOpt = conf.showAndWait();
                if (confOpt.isPresent() && confOpt.get().equals("Sim")) {
                    Doctor d = candidates.get(0); // primeiro candidato para fila
                    scheduler.schedule(patient.getId(), d.getId(), start, end); // entra em espera se conflito
                    output.appendText("Adicionado à lista de espera para " + d.getName() + "\n");
                } else {
                    output.appendText("Agendamento não realizado.\n");
                }
            }
        } catch (Exception ex) {
            output.appendText("Erro ao agendar: " + ex.getMessage() + "\n");
        }
    }

    @FXML
    private void onList() {
        List<Appointment> list = scheduler.listAppointments();
        if (list.isEmpty()) { output.appendText("Nenhuma consulta.\n"); return; }
        for (Appointment a : list) {
            Patient p = patientDAO.findById(a.getPatientId());
            Doctor d = doctorDAO.findById(a.getDoctorId());
            output.appendText(formatAppointmentFriendly(a, p, d) + "\n");
        }
    }

    private LocalTime promptForTime(String header) {
        while (true) {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Agendar consulta");
            dlg.setHeaderText(header);
            Optional<String> opt = dlg.showAndWait();
            if (opt.isEmpty()) return null;
            String s = opt.get().trim();
            try {
                if (s.matches("\\d{1,2}")) {
                    int h = Integer.parseInt(s);
                    if (h >= 0 && h < 24) return LocalTime.of(h, 0);
                } else if (s.matches("\\d{1,2}:\\d{2}")) {
                    String[] ss = s.split(":");
                    int h = Integer.parseInt(ss[0]);
                    int m = Integer.parseInt(ss[1]);
                    if (h >= 0 && h < 24 && m >= 0 && m < 60) return LocalTime.of(h, m);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static class WaitingEntry {
        int patientId;
        LocalDateTime start;
        LocalDateTime end;
        String specialty;
        WaitingEntry(int patientId, LocalDateTime start, LocalDateTime end, String specialty) {
            this.patientId = patientId; this.start = start; this.end = end; this.specialty = specialty;
        }
    }

    private String formatAppointmentFriendly(Appointment a, Patient p, Doctor d) {
        LocalDateTime s = a.getStart();
        LocalDateTime e = a.getEnd();
        int day = s.getDayOfMonth();
        int month = s.getMonthValue();
        String[] months = {"janeiro","fevereiro","março","abril","maio","junho","julho","agosto","setembro","outubro","novembro","dezembro"};
        String monthName = months[month-1];
        String startTime = String.format("%dh", s.getHour()) + (s.getMinute() == 0 ? "" : String.format("%02d", s.getMinute()));
        String endTime = String.format("%dh", e.getHour()) + (e.getMinute() == 0 ? "" : String.format("%02d", e.getMinute()));
        String patientName = p != null ? p.getName() : ("Paciente " + a.getPatientId());
        String doctorDisplay;
        if (d != null) {
            doctorDisplay = "Dr. " + d.getName() + " (" + d.getSpecialty() + ")";
        } else {
            doctorDisplay = "Dr. " + a.getDoctorId();
        }
        return String.format("Paciente %s agendado com %s, dia %d de %s, das %s às %s.", patientName, doctorDisplay, day, monthName, startTime, endTime);
    }


    @FXML
    private void onCancel() {
        ChoiceDialog<String> choice = new ChoiceDialog<>("Por ID", List.of("Por ID", "Por paciente", "Por médico"));
        choice.setTitle("Cancelar consulta");
        choice.setHeaderText("Escolha o método de cancelamento:");
        Optional<String> choiceOpt = choice.showAndWait();
        if (choiceOpt.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
        String method = choiceOpt.get();
        try {
            if (method.equals("Por ID")) {
                TextInputDialog idDlg = new TextInputDialog();
                idDlg.setTitle("Cancelar por ID");
                idDlg.setHeaderText("Id da consulta a cancelar:");
                Optional<String> idOpt = idDlg.showAndWait();
                if (idOpt.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
                int id = Integer.parseInt(idOpt.get().trim());
                boolean ok = scheduler.cancelAppointment(id);
                output.appendText(ok ? "Cancelada.\n" : "Não encontrada.\n");
            } else if (method.equals("Por paciente")) {
                TextInputDialog nameDlg = new TextInputDialog();
                nameDlg.setTitle("Cancelar por paciente");
                nameDlg.setHeaderText("Nome do paciente:");
                Optional<String> nameOpt = nameDlg.showAndWait();
                if (nameOpt.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
                String name = nameOpt.get().trim().toLowerCase();
                List<Patient> ps = patientDAO.listAll();
                Patient match = null;
                for (Patient p : ps) {
                    if (p.getName().toLowerCase().equals(name)) { match = p; break; }
                }
                if (match == null) { output.appendText("Paciente não encontrado. Use 'Mostrar IDs' se necessário.\n"); return; }
                List<Appointment> appts = scheduler.listAppointments();
                List<Appointment> found = new java.util.ArrayList<>();
                for (Appointment a : appts) if (a.getPatientId() == match.getId()) found.add(a);
                if (found.isEmpty()) { output.appendText("Nenhuma consulta encontrada para esse paciente.\n"); return; }
                if (found.size() == 1) {
                    boolean ok = scheduler.cancelAppointment(found.get(0).getId());
                    output.appendText(ok ? "Cancelada.\n" : "Não encontrada.\n");
                } else {
                    List<String> options = new java.util.ArrayList<>();
                    for (Appointment a : found) {
                        Patient pp = patientDAO.findById(a.getPatientId());
                        Doctor dd = doctorDAO.findById(a.getDoctorId());
                        options.add(formatAppointmentFriendly(a, pp, dd));
                    }
                    ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
                    choose.setTitle("Escolha consulta");
                    choose.setHeaderText("Qual consulta deseja cancelar?");
                    Optional<String> chosen = choose.showAndWait();
                    if (chosen.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
                    int idx = options.indexOf(chosen.get());
                    int id = found.get(idx).getId();
                    boolean ok = scheduler.cancelAppointment(id);
                    output.appendText(ok ? "Cancelada.\n" : "Não encontrada.\n");
                }
            } else { // Por médico
                TextInputDialog nameDlg = new TextInputDialog();
                nameDlg.setTitle("Cancelar por médico");
                nameDlg.setHeaderText("Nome do médico:");
                Optional<String> nameOpt = nameDlg.showAndWait();
                if (nameOpt.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
                String name = nameOpt.get().trim().toLowerCase();
                List<Doctor> ds = doctorDAO.listAll();
                Doctor match = null;
                for (Doctor d : ds) { if (d.getName().toLowerCase().equals(name)) { match = d; break; } }
                if (match == null) { output.appendText("Médico não encontrado. Use 'Mostrar IDs' se necessário.\n"); return; }
                List<Appointment> appts = scheduler.listAppointments();
                List<Appointment> found = new java.util.ArrayList<>();
                for (Appointment a : appts) if (a.getDoctorId() == match.getId()) found.add(a);
                if (found.isEmpty()) { output.appendText("Nenhuma consulta encontrada para esse médico.\n"); return; }
                if (found.size() == 1) {
                    boolean ok = scheduler.cancelAppointment(found.get(0).getId());
                    output.appendText(ok ? "Cancelada.\n" : "Não encontrada.\n");
                } else {
                    List<String> options = new java.util.ArrayList<>();
                    for (Appointment a : found) {
                        Patient pp = patientDAO.findById(a.getPatientId());
                        options.add(formatAppointmentFriendly(a, pp, match));
                    }
                    ChoiceDialog<String> choose = new ChoiceDialog<>(options.get(0), options);
                    choose.setTitle("Escolha consulta");
                    choose.setHeaderText("O médico " + match.getName() + " possui consultas com os seguintes pacientes:\nEscolha qual cancelar:");
                    Optional<String> chosen = choose.showAndWait();
                    if (chosen.isEmpty()) { output.appendText("Cancelamento cancelado.\n"); return; }
                    int idx = options.indexOf(chosen.get());
                    int id = found.get(idx).getId();
                    boolean ok = scheduler.cancelAppointment(id);
                    output.appendText(ok ? "Cancelada.\n" : "Não encontrada.\n");
                }
            }
        } catch (Exception ex) {
            output.appendText("Erro: " + ex.getMessage() + "\n");
        }
    }

    @FXML
    private void onShowIds() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pacientes (nomes):\n");
        for (Patient p : patientDAO.listAll()) sb.append("- ").append(p.getName()).append('\n');
        sb.append("\nMédicos:\n");
        for (Doctor d : doctorDAO.listAll()) sb.append("- ").append(d.getName()).append(" (" + d.getSpecialty() + ")").append('\n');
        sb.append("\nConsultas:\n");
        for (Appointment a : scheduler.listAppointments()) {
            Patient p = patientDAO.findById(a.getPatientId());
            Doctor d = doctorDAO.findById(a.getDoctorId());
            sb.append("- ").append(formatAppointmentFriendly(a, p, d)).append('\n');
        }
        output.appendText(sb.toString());
    }

    @FXML
    private void onClear() {
        output.clear();
    }

    @FXML
    private void onWaiting() {
        StringBuilder sb = new StringBuilder();
        var wait = scheduler.listWaiting();
        if ((wait == null || wait.isEmpty()) && specialtyWaiting.isEmpty()) { output.appendText("Lista de espera vazia.\n"); return; }
        if (wait != null && !wait.isEmpty()) {
            sb.append("Espera por médico específico:\n");
            for (var w : wait) {
                Patient p = patientDAO.findById(w.patientId);
                Doctor d = doctorDAO.findById(w.doctorId);
                if (p != null && d != null)
                    sb.append("- ").append(p.getName()).append(" aguardando por Dr. ").append(d.getName()).append(" (" + d.getSpecialty() + ")").append(", " + w.start.toLocalDate() + " " + w.start.toLocalTime() + " - " + w.end.toLocalTime()).append('\n');
                else
                    sb.append("- patient=" + w.patientId + ", doctor=" + w.doctorId + ", " + w.start + " - " + w.end + "\n");
            }
        }
        if (!specialtyWaiting.isEmpty()) {
            sb.append("Espera por especialidade:\n");
            for (var e : specialtyWaiting.entrySet()) {
                String spec = e.getKey();
                Queue<WaitingEntry> q = e.getValue();
                for (WaitingEntry we : q) {
                    Patient p = patientDAO.findById(we.patientId);
                    sb.append("- ").append(p != null ? p.getName() : ("Paciente " + we.patientId))
                      .append(" (especialidade: ").append(spec).append(") dia ")
                      .append(we.start.toLocalDate()).append(" ")
                      .append(we.start.toLocalTime()).append(" - ")
                      .append(we.end.toLocalTime()).append('\n');
                }
            }
        }
        output.appendText(sb.toString());
    }
}
