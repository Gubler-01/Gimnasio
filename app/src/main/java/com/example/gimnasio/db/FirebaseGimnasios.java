package com.example.gimnasio.db;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.example.gimnasio.LogIn;
import com.google.firebase.Firebase;
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

    private Firebase firebase;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public FirebaseGimnasios(Context context){
        FirebaseApp.initializeApp(context);
        firebaseDatabase= FirebaseDatabase.getInstance(FirebaseApp.getInstance());
        databaseReference = firebaseDatabase.getReference();
    }

    public boolean addRegistroGimnasio(int id, String name,
                                     String email,String fecha_ingreso,String costo,
                                     String foto,String video, String telefono,
                                     String latitud, String longitud,int idUsuario){
        Gimnasios p = new Gimnasios();
        p.setId(id);
        p.setNombre(name);
        p.setEmail(email);
        p.setFecha_ingreso(fecha_ingreso);
        p.setCosto(costo);
        p.setFoto(foto);
        p.setVideo(video);
        p.setTelefono(telefono);
        p.setLatitud(latitud);
        p.setLongitud(longitud);
        p.setIdUsuario(idUsuario);
        databaseReference.child("Gimnasios").child(String.valueOf(p.getId())).setValue(p);
        return true;
    }

    public void eliminarGimnasio(Gimnasios gimSelected){
        databaseReference.child("Gimnasios").child(String.valueOf(gimSelected.getId())).removeValue();

    }

    public boolean updateRegistroGimnasio(Gimnasios gimSelected, String name,
                                       String email,String fecha_ingreso,String costo,
                                       String foto,String video, String telefono,
                                       String latitud, String longitud){
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
        databaseReference.child("Gimnasios").child(String.valueOf(p.getId())).setValue(p);
        return true;
    }

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
                    if (gimnasios.getIdUsuario() == LogIn.usr.getId()){
                        listener.onGimnasioFound(gimnasios);
                    }else {
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
                listener.onError(error.toException());  // Manejo de errores
            }
        });
    }

    public interface OnGimnasiosFoundListener {
        void onGimnasiosFound(List<Gimnasios> cuentas);  // Cuando se encuentran todas las cuentas
        void onGimnasiosNotFound();                   // Cuando no hay cuentas
        void onError(Exception e);                  // Manejo de errores
    }

    public interface OnGimnasioFoundListener {
        void onGimnasioFound(Gimnasios gimnasios);
        void onGimnasioNotFound();
        void onError(Exception e);
    }

    public void getGimnasiosVigenteUsuario (final OnVig listener ){
        final ArrayList<String> nombres = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        databaseReference.child("Gimnasios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gimnasios gimnasios = snapshot.getValue(Gimnasios.class);
                        assert gimnasios != null;

                        nombres.add("NOMBRE "+gimnasios.getNombre()
                                +"\n"+"COSTO "+gimnasios.getCosto()
                                +"\n"+"EMAIL "+gimnasios.getEmail()
                                +"\n"+"TELEFONO "+gimnasios.getTelefono()
                                +"\n"+"LATITUD "+gimnasios.getLatitud()
                                +"\n"+"LONGITUD "+gimnasios.getLongitud());
                        ids.add(String.valueOf(gimnasios.getId()));
                        listener.onGimnasioFound(nombres,ids);

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


    public void getGimnasiosVigente (final OnVig listener ){
        final ArrayList<String> nombres = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        databaseReference.child("Gimnasios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gimnasios gimnasios = snapshot.getValue(Gimnasios.class);
                        assert gimnasios != null;
                        if (gimnasios.getIdUsuario() == LogIn.usr.getId()){
                            nombres.add("NOMBRE "+gimnasios.getNombre()
                                    +"\n"+"COSTO "+gimnasios.getCosto()
                                    +"\n"+"EMAIL "+gimnasios.getEmail()
                                    +"\n"+"TELEFONO "+gimnasios.getTelefono()
                                    +"\n"+"LATITUD "+gimnasios.getLatitud()
                                    +"\n"+"LONGITUD "+gimnasios.getLongitud());
                            ids.add(String.valueOf(gimnasios.getId()));
                            listener.onGimnasioFound(nombres,ids);
                        }else {
                            listener.onGimnasioNotFound();
                        }
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

    public interface OnVig {
        void onGimnasioFound(ArrayList<String> nombres,ArrayList<String> ids);
        void onGimnasioNotFound();
        void onError(Exception e);
    }


}
