package com.example.gimnasio.db;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.example.gimnasio.LogIn;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseGimnasios {
    private final ArrayList<Gimnasios> lgimnasios = new ArrayList<>();
    ArrayAdapter<Gimnasios> gimnasiosArrayAdapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public FirebaseGimnasios(Context context) {
        FirebaseApp.initializeApp(context);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    // Método ajustado con callback
    public void addRegistroGimnasio(int id, String name, String email, String fecha_ingreso, String costo,
                                    String foto, String video, String telefono, String latitud, String longitud, int idUsuario,
                                    OnRegistroCompleteListener listener) {
        Gimnasios p = new Gimnasios();
        p.setId(id);
        p.setNombre(name);
        p.setEmail(email);
        p.setFecha_ingreso(fecha_ingreso);
        p.setCosto(costo);
        p.setFoto(foto); // Base64 de la imagen
        p.setVideo(video); // Base64 del video
        p.setTelefono(telefono);
        p.setLatitud(latitud);
        p.setLongitud(longitud);
        p.setIdUsuario(idUsuario);

        // Usar el id como clave (puedes cambiar a push() para un ID único)
        databaseReference.child("Gimnasios").child(String.valueOf(p.getId())).setValue(p)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onRegistroComplete(true);
                    } else {
                        listener.onRegistroError(task.getException());
                    }
                });
    }

    public void eliminarGimnasio(Gimnasios gimSelected) {
        databaseReference.child("Gimnasios").child(String.valueOf(gimSelected.getId())).removeValue();
    }

    public void updateRegistroGimnasio(Gimnasios gimSelected, String name, String email, String fecha_ingreso, String costo,
                                       String foto, String video, String telefono, String latitud, String longitud,
                                       OnRegistroCompleteListener listener) {
        Gimnasios p = new Gimnasios();
        p.setId(gimSelected.getId());
        p.setNombre(name);
        p.setEmail(email);
        p.setFecha_ingreso(fecha_ingreso);
        p.setCosto(costo);
        p.setFoto(foto);
        p.setVideo(video);
        p.setTelefono(telefono);
        p.setLatitud(latitud);
        p.setLongitud(longitud);
        p.setIdUsuario(LogIn.usr.getId());

        databaseReference.child("Gimnasios").child(String.valueOf(p.getId())).setValue(p)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onRegistroComplete(true);
                    } else {
                        listener.onRegistroError(task.getException());
                    }
                });
    }

    // Métodos existentes sin cambios (getRegistroGimnasioUsuario, getRegistroGimnasio, getAllGimnasios, etc.)
    public void getRegistroGimnasioUsuario(int id, final OnGimnasioFoundListener listener) {
        databaseReference.child("Gimnasios").child(String.valueOf(id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gimnasios gimnasios = dataSnapshot.getValue(Gimnasios.class);
                    listener.onGimnasioFound(gimnasios);
                } else {
                    listener.onGimnasioNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void getRegistroGimnasio(int id, final OnGimnasioFoundListener listener) {
        databaseReference.child("Gimnasios").child(String.valueOf(id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gimnasios gimnasios = dataSnapshot.getValue(Gimnasios.class);
                    if (gimnasios.getIdUsuario() == LogIn.usr.getId()) {
                        listener.onGimnasioFound(gimnasios);
                    } else {
                        listener.onGimnasioNotFound();
                    }
                } else {
                    listener.onGimnasioNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void getAllGimnasios(final OnGimnasiosFoundListener listener) {
        databaseReference.child("Gimnasios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Gimnasios> gimnasiosList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gimnasios gimnasios = snapshot.getValue(Gimnasios.class);
                        gimnasiosList.add(gimnasios);
                    }
                    listener.onGimnasiosFound(gimnasiosList);
                } else {
                    listener.onGimnasiosNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void getGimnasiosVigenteUsuario(final OnVig listener) {
        final ArrayList<String> nombres = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        databaseReference.child("Gimnasios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gimnasios gimnasios = snapshot.getValue(Gimnasios.class);
                        assert gimnasios != null;
                        nombres.add("NOMBRE " + gimnasios.getNombre()
                                + "\n" + "COSTO " + gimnasios.getCosto()
                                + "\n" + "EMAIL " + gimnasios.getEmail()
                                + "\n" + "TELEFONO " + gimnasios.getTelefono()
                                + "\n" + "LATITUD " + gimnasios.getLatitud()
                                + "\n" + "LONGITUD " + gimnasios.getLongitud());
                        ids.add(String.valueOf(gimnasios.getId()));
                        listener.onGimnasioFound(nombres, ids);
                    }
                } else {
                    listener.onGimnasioNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void getGimnasiosVigente(final OnVig listener) {
        final ArrayList<String> nombres = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        databaseReference.child("Gimnasios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gimnasios gimnasios = snapshot.getValue(Gimnasios.class);
                        assert gimnasios != null;
                        if (gimnasios.getIdUsuario() == LogIn.usr.getId()) {
                            nombres.add("NOMBRE " + gimnasios.getNombre()
                                    + "\n" + "COSTO " + gimnasios.getCosto()
                                    + "\n" + "EMAIL " + gimnasios.getEmail()
                                    + "\n" + "TELEFONO " + gimnasios.getTelefono()
                                    + "\n" + "LATITUD " + gimnasios.getLatitud()
                                    + "\n" + "LONGITUD " + gimnasios.getLongitud());
                            ids.add(String.valueOf(gimnasios.getId()));
                            listener.onGimnasioFound(nombres, ids);
                        }
                    }
                    if (nombres.isEmpty()) {
                        listener.onGimnasioNotFound();
                    }
                } else {
                    listener.onGimnasioNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    // Interfaz para callback de registro
    public interface OnRegistroCompleteListener {
        void onRegistroComplete(boolean success);
        void onRegistroError(Exception e);
    }

    // Interfaces existentes
    public interface OnGimnasiosFoundListener {
        void onGimnasiosFound(List<Gimnasios> cuentas);
        void onGimnasiosNotFound();
        void onError(Exception e);
    }

    public interface OnGimnasioFoundListener {
        void onGimnasioFound(Gimnasios gimnasios);
        void onGimnasioNotFound();
        void onError(Exception e);
    }

    public interface OnVig {
        void onGimnasioFound(ArrayList<String> nombres, ArrayList<String> ids);
        void onGimnasioNotFound();
        void onError(Exception e);
    }
}