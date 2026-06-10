package com.example.finanzasclaras;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class TarjetasViewModel extends ViewModel {

    private final MutableLiveData<List<Tarjeta>> tarjetas = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final FirestoreRepository repo = FirestoreRepository.getInstance();

    public TarjetasViewModel() {
        cargarTarjetas();
    }

    private void cargarTarjetas() {
        repo.getTarjetas(new FirestoreRepository.OnListListener<Tarjeta>() {
            @Override
            public void onSuccess(List<Tarjeta> lista) {
                tarjetas.setValue(lista);
            }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void agregarTarjeta(Tarjeta tarjeta) {
        repo.addTarjeta(tarjeta, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() { /* LiveData se actualiza solo */ }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void eliminarTarjeta(String tarjetaId) {
        repo.deleteTarjeta(tarjetaId, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() { /* LiveData se actualiza solo */ }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }
    public void actualizarTarjeta(Tarjeta tarjeta) {
        repo.updateTarjeta(tarjeta, new FirestoreRepository.OnActionListener() {
            @Override public void onSuccess() {}
            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
        });
    }

    public LiveData<List<Tarjeta>> getTarjetas() { return tarjetas; }
    public LiveData<String> getError() { return error; }
}