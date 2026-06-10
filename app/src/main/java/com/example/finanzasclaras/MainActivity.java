package com.example.finanzasclaras;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 1. Crear el canal de notificaciones (Requerido en Android)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "alertas_pagos",
                    "Alertas de Pago",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Recordatorios para pagar tarjetas");
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Pedir permiso de notificaciones (Requerido para Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        androidx.work.PeriodicWorkRequest recordatorioRequest =
                new androidx.work.PeriodicWorkRequest.Builder(
                        RecordatorioWorker.class,
                        1, // Se ejecutará 1 vez...
                        java.util.concurrent.TimeUnit.DAYS // ...cada día
                ).build();

        // Le decimos a WorkManager que lo encole, asegurando que no se duplique
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RutinaRevisarPagos",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                recordatorioRequest
        );

        // --- LÓGICA PARA OCULTAR LA BARRA INFERIOR ---
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // 👇 ¡AQUÍ ESTÁ LA CORRECCIÓN! Agregamos el historialFragment 👇
            if (destination.getId() == R.id.homeFragment ||
                    destination.getId() == R.id.personasFragment ||
                    destination.getId() == R.id.tarjetasFragment ||
                    destination.getId() == R.id.historialFragment) {

                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });
    }
}