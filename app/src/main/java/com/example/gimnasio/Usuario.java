package com.example.gimnasio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.example.gimnasio.db.FirebaseGimnasios;
import com.example.gimnasio.db.Gimnasios;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class Usuario extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<String> registros;
    private ArrayList<String> idS;
    private FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        // Configuración de la pantalla para EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Cargar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_mapDialogo);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicialización de Firebase y configuración de lista
        firebaseGimnasios = new FirebaseGimnasios(this);
        firebaseGimnasios.getGimnasiosVigenteUsuario(new FirebaseGimnasios.OnVig() {
            @Override
            public void onGimnasioFound(ArrayList<String> nombres, ArrayList<String> ids) {
                registros = nombres;
                idS = ids;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Usuario.this, android.R.layout.simple_list_item_1, registros);
                ListView list = findViewById(R.id.listaCuentaUsuario);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("MissingInflatedId")
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        View dialogView = LayoutInflater.from(Usuario.this).inflate(R.layout.dialogo_gimnasios, null);
                        TextView textView = dialogView.findViewById(R.id.textViewInfoL);
                        textView.setText(registros.get(i));
                        long id = Long.parseLong(idS.get(i));

                        firebaseGimnasios.getRegistroGimnasioUsuario((int) id, new FirebaseGimnasios.OnGimnasioFoundListener() {
                            @Override
                            public void onGimnasioFound(Gimnasios gimnasios) {
                                ImageView imagen = dialogView.findViewById(R.id.imageViewListaLibro);
                                cargarImagenEnImageView(gimnasios.getFoto(), imagen);

                                // Mostrar información en el diálogo
                                TextView infoText = dialogView.findViewById(R.id.textViewInfoL);
                                infoText.setText(gimnasios.getNombre()); // Ajusta según el modelo Gimnasios

                                AlertDialog.Builder dialogo = new AlertDialog.Builder(Usuario.this);
                                dialogo.setTitle("Gimnasio");
                                dialogo.setView(dialogView);
                                dialogo.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                                dialogo.show();

                                // Mover la cámara al marcador en el mapa principal
                                if (mMap != null) {
                                    LatLng ubi = new LatLng(Double.parseDouble(gimnasios.getLatitud()), Double.parseDouble(gimnasios.getLongitud()));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi, 10));
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(ubi).title("Dirección seleccionada"));
                                    enableMyLocation();
                                }
                            }

                            @Override
                            public void onGimnasioNotFound() {
                                Toast.makeText(Usuario.this, "Gimnasio no encontrado", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(Usuario.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onGimnasioNotFound() {
                Toast.makeText(Usuario.this, "No se encontraron gimnasios", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(Usuario.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cargarImagenEnImageView(String url, ImageView imageView) {
        if (imageView != null) {
            Glide.with(this)
                    .load(url)
                    .into(imageView);
        } else {
            Toast.makeText(this, "Error al cargar la imagen: ImageView nulo", Toast.LENGTH_SHORT).show();
        }
    }
}