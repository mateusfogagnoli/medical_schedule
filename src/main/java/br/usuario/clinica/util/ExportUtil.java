package br.usuario.clinica.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import br.usuario.clinica.model.Appointment;

public class ExportUtil {
    public static void exportAppointmentsCsv(List<Appointment> list, File outFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
            pw.println("id,patientId,doctorId,start,end");
            for (Appointment a : list) {
                pw.printf("%d,%d,%d,%s,%s\n",
                        a.getId(), a.getPatientId(), a.getDoctorId(), a.getStart().toString(), a.getEnd().toString());
            }
        }
    }
}
