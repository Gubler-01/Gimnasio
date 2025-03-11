package com.example.gimnasio.db;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUsuarios {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    public FirebaseUsuarios(Context context) {
        FirebaseApp.initializeApp(context);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public void addRegistroCuenta(int id, String name, String email, String fecha_ingreso,
                                  String password, String fotoBase64, String genero, String tipo,
                                  OnRegistroCompleteListener listener) {
        Usuarios p = new Usuarios();
        p.setId(id);
        p.setNombre(name);
        p.setEmail(email);
        p.setFecha_ingreso(fecha_ingreso);
        p.setGenero(genero);
        p.setImagen(fotoBase64); // Imagen como Base64
        p.setTipo(tipo);
        // No seteamos password, Firebase Auth lo maneja

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso en Authentication
                        String authUserId = mAuth.getCurrentUser().getUid(); // UID de Firebase Auth (no usado como clave aquí)
                        databaseReference.child("Usuarios").child(String.valueOf(p.getId())).setValue(p)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        listener.onRegistroComplete(true);
                                    } else {
                                        listener.onRegistroError(dbTask.getException());
                                    }
                                });
                    } else {
                        listener.onRegistroError(task.getException());
                    }
                });
    }

    public void getRegistroCuenta(String email, final OnUsuariosFoundListener listener) {
        databaseReference.child("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Usuarios> usuariosList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuarios usuario = snapshot.getValue(Usuarios.class);
                    if (usuario != null && usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(email)) {
                        usuariosList.add(usuario);
                    }
                }

                if (!usuariosList.isEmpty()) {
                    listener.onUsuariosFound(usuariosList);
                } else {
                    listener.onUsuariosNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public interface OnUsuariosFoundListener {
        void onUsuariosFound(List<Usuarios> usuarios);
        void onUsuariosNotFound();
        void onError(Exception e);
    }

    public interface OnRegistroCompleteListener {
        void onRegistroComplete(boolean success);
        void onRegistroError(Exception e);
    }
}