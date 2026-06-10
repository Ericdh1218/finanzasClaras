package com.example.finanzasclaras;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class RecordatorioWorker extends Worker {

    public RecordatorioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 1. Calculamos los 3 futuros distintos
            Calendar cal5 = Calendar.getInstance(); cal5.add(Calendar.DAY_OF_MONTH, 5);
            int dia5 = cal5.get(Calendar.DAY_OF_MONTH);
            boolean ultimoDia5 = (dia5 == cal5.getActualMaximum(Calendar.DAY_OF_MONTH));

            Calendar cal3 = Calendar.getInstance(); cal3.add(Calendar.DAY_OF_MONTH, 3);
            int dia3 = cal3.get(Calendar.DAY_OF_MONTH);
            boolean ultimoDia3 = (dia3 == cal3.getActualMaximum(Calendar.DAY_OF_MONTH));

            Calendar cal1 = Calendar.getInstance(); cal1.add(Calendar.DAY_OF_MONTH, 1);
            int dia1 = cal1.get(Calendar.DAY_OF_MONTH);
            boolean ultimoDia1 = (dia1 == cal1.getActualMaximum(Calendar.DAY_OF_MONTH));

            // 2. Conectamos a Firebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            QuerySnapshot snapshot = Tasks.await(db.collection("tarjetas").get());

            // 3. Revisamos las tarjetas
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Boolean esQuincenal = doc.getBoolean("esQuincenal");
                if (esQuincenal == null) esQuincenal = false;

                String nombreTarjeta = doc.getString("nombre");
                int diasFaltantes = -1;

                if (esQuincenal) {
                    // Tarjetas Quincenales
                    if (dia5 == 15 || ultimoDia5) diasFaltantes = 5;
                    else if (dia3 == 15 || ultimoDia3) diasFaltantes = 3;
                    else if (dia1 == 15 || ultimoDia1) diasFaltantes = 1;
                } else {
                    // Tarjetas Mensuales
                    Long fechaPagoLong = doc.getLong("fechaPago");
                    if (fechaPagoLong != null) {
                        int diaPago = fechaPagoLong.intValue();

                        // Ajuste inteligente para meses cortos (ej. febrero o meses de 30 días)
                        int diaPagoReal5 = Math.min(diaPago, cal5.getActualMaximum(Calendar.DAY_OF_MONTH));
                        int diaPagoReal3 = Math.min(diaPago, cal3.getActualMaximum(Calendar.DAY_OF_MONTH));
                        int diaPagoReal1 = Math.min(diaPago, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));

                        if (diaPagoReal5 == dia5) diasFaltantes = 5;
                        else if (diaPagoReal3 == dia3) diasFaltantes = 3;
                        else if (diaPagoReal1 == dia1) diasFaltantes = 1;
                    }
                }

                // 4. Armamos el mensaje dependiendo de la urgencia
                if (diasFaltantes != -1) {
                    String mensaje;
                    if (diasFaltantes == 1) {
                        mensaje = "¡MAÑANA es la fecha límite de pago de tu tarjeta " + nombreTarjeta + "!";
                    } else {
                        mensaje = "Faltan " + diasFaltantes + " días para la fecha límite de pago de tu tarjeta " + nombreTarjeta + ".";
                    }

                    int notificationId = (int) System.currentTimeMillis() % 10000;
                    mostrarNotificacion(notificationId, "💳 Pago próximo: " + nombreTarjeta, mensaje);
                }
            }

            return Result.success();

        } catch (Exception e) {
            Log.e("CuentasClarasWorker", "Error revisando fechas", e);
            return Result.failure();
        }
    }

    private void mostrarNotificacion(int id, String titulo, String mensaje) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "alertas_pagos")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(id, builder.build());
        }
    }
}