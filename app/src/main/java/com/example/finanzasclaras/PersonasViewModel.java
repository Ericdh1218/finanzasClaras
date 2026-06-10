package com.example.finanzasclaras;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PersonasViewModel extends ViewModel {

    private final MutableLiveData<List<Persona>> personas = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final FirestoreRepository repo = FirestoreRepository.getInstance();

    public PersonasViewModel() {
        cargarPersonas();
    }

    private void cargarPersonas() {
        repo.getPersonas(new FirestoreRepository.OnListListener<Persona>() {
            @Override
            public void onSuccess(List<Persona> lista) {
                personas.setValue(lista);
            }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }
    public void actualizarPersona(Persona persona) {
        repo.updatePersona(persona, new FirestoreRepository.OnActionListener() {
            @Override public void onSuccess() {}
            @Override public void onError(Exception e) { error.setValue(e.getMessage()); }
        });
    }
    public void agregarPersona(Persona persona) {
        repo.addPersona(persona, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() { /* LiveData se actualiza solo */ }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void eliminarPersona(String personaId) {
        repo.deletePersona(personaId, new FirestoreRepository.OnActionListener() {
            @Override
            public void onSuccess() { /* LiveData se actualiza solo */ }
            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    // Calcula deuda total de una persona sumando sus compras pendientes
    public double getTotalDeuda(List<Compra> compras) {
        double total = 0;
        for (Compra c : compras) {
            total += c.getSaldoPendiente();
        }
        return total;
    }

    public LiveData<List<Persona>> getPersonas() { return personas; }
    public LiveData<String> getError() { return error; }
}