package com.example.gimnasio.ui.buscar;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gimnasio.R;
import com.example.gimnasio.databinding.FragmentCrearBinding;
import com.example.gimnasio.db.FirebaseGimnasios;
import com.example.gimnasio.db.Gimnasios;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class BuscarFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private TextView editTextId, editTextNombre, editTextEmail, editTextTelefono, editTextCosto;
    private TextView textViewLatitud, textViewLongitud;
    private ImageView imageView;
    private Button btnBuscar;
    private String a = "", d = "", sex = "";
    public FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;

    private BuscarViewModel mViewModel;

    public static BuscarFragment newInstance() {
        return new BuscarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buscar, container, false);
        firebaseGimnasios = new FirebaseGimnasios(getContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_mapBuscar);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            // Maneja el caso donde mapFragment es null
            Log.e("CrearFragment", "El fragmento de mapa es null");
        }
        componentes(root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BuscarViewModel.class);
        // TODO: Use the ViewModel
    }
    private void componentes(View root) {
        editTextComponentes(root);
        botonComponentes(root);
    }

    private void editTextComponentes(View root) {
        editTextId = root.findViewById(R.id.idBuscar);
        editTextNombre = root.findViewById(R.id.nombreBuscar);
        editTextEmail = root.findViewById(R.id.emailBuscar);
        editTextTelefono = root.findViewById(R.id.telefonoBuscar);
        editTextCosto = root.findViewById(R.id.costoBuscar);
        textViewLatitud = root.findViewById(R.id.latitudBuscar);
        textViewLongitud = root.findViewById(R.id.longitudBuscar);
    }

    private void botonComponentes(View root) {
        btnBuscar = root.findViewById(R.id.btnBuscarBuscar);
        btnBuscar.setOnClickListener(this);
        imageView = root.findViewById(R.id.imagenBuscar);
        imageView.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnBuscarBuscar) {
            int id = Integer.parseInt(editTextId.getText().toString());
            firebaseGimnasios.getRegistroGimnasio(id, new FirebaseGimnasios.OnGimnasioFoundListener() {
                @Override
                public void onGimnasioFound(Gimnasios gimnasios) {
                    editTextNombre.setText(gimnasios.getNombre());
                    editTextEmail.setText(gimnasios.getEmail());
                    editTextTelefono.setText(gimnasios.getTelefono());
                    textViewLatitud.setText(gimnasios.getLatitud());
                    textViewLongitud.setText(gimnasios.getLongitud());
                    editTextCosto.setText(gimnasios.getCosto());
                    LatLng ubi = new LatLng(Double.parseDouble(textViewLatitud.getText().toString()), Double.parseDouble(textViewLongitud.getText().toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi, 10));
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(ubi).title("Dirección seleccionada"));
                    cargarImagenEnImageView(gimnasios.getFoto(),imageView);
                }

                @Override
                public void onGimnasioNotFound() {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        }

    }





    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng toluca = new LatLng(19.2826, -99.6557);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toluca, 15));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                double latitud = latLng.latitude;
                double longitud = latLng.longitude;
                textViewLatitud.setText(String.valueOf(latitud));
                textViewLongitud.setText(String.valueOf(longitud));
                mMap.addMarker(new MarkerOptions().position(latLng).title("Coordenadas seleccionadas"));
            }
        });


    }


    public void cargarImagenEnImageView(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // Decodificar la cadena Base64 a un array de bytes
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                // Convertir el array de bytes a un Bitmap
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                // Establecer el Bitmap en el ImageView
                imageView.setImageBitmap(decodedBitmap);
                Log.d("BuscarFragment", "Imagen cargada correctamente desde Base64");
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al cargar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BuscarFragment", "Error al decodificar imagen Base64: ", e);
            }
        } else {
            Toast.makeText(getContext(), "No hay imagen disponible", Toast.LENGTH_SHORT).show();
            Log.w("BuscarFragment", "La cadena Base64 está vacía o es nula");
        }
    }



}