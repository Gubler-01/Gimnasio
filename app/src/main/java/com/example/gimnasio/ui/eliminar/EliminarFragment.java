package com.example.gimnasio.ui.eliminar;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class EliminarFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {
    private TextView editTextId, editTextNombre, editTextEmail, editTextTelefono, editTextCosto;
    private TextView textViewLatitud, textViewLongitud;
    private ImageView imageView;
    private Button btnLimpiar,btnBuscar,btnEliminar;
    public FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;

    private EliminarViewModel mViewModel;

    public static EliminarFragment newInstance() {
        return new EliminarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_eliminar, container, false);
        firebaseGimnasios = new FirebaseGimnasios(getContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_mapEliminar);
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
        mViewModel = new ViewModelProvider(this).get(EliminarViewModel.class);
        // TODO: Use the ViewModel
    }
    private void componentes(View root) {
        editTextComponentes(root);
        botonComponentes(root);
    }

    private void editTextComponentes(View root) {
        editTextId = root.findViewById(R.id.idEliminar);
        editTextNombre = root.findViewById(R.id.nombreEliminar);
        editTextEmail = root.findViewById(R.id.emailEliminar);
        editTextTelefono = root.findViewById(R.id.telefonoEliminar);
        editTextCosto = root.findViewById(R.id.costoEliminar);
        textViewLatitud = root.findViewById(R.id.latitudEliminar);
        textViewLongitud = root.findViewById(R.id.longitudEliminar);
    }

    private void botonComponentes(View root) {
        btnBuscar = root.findViewById(R.id.btnBuscarEliminar);
        imageView = root.findViewById(R.id.imagenEliminar);
        btnLimpiar = root.findViewById(R.id.btnelLimpiar);
        btnEliminar = root.findViewById(R.id.btnelEliminar);
        btnEliminar.setOnClickListener(this);
        imageView.setOnClickListener(this);
        btnBuscar.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnBuscarEliminar) {
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
        } else if (view.getId() == R.id.btnelLimpiar) {
            limpiar();
        } else if (view.getId() == R.id.btnelEliminar) {
            int id = Integer.parseInt(editTextId.getText().toString());
            firebaseGimnasios.getRegistroGimnasio(id, new FirebaseGimnasios.OnGimnasioFoundListener() {
                @Override
                public void onGimnasioFound(Gimnasios gimnasios) {
                    firebaseGimnasios.eliminarGimnasio(gimnasios);
                    Toast.makeText(getContext(), "Eliminado con exito", Toast.LENGTH_SHORT).show();
                    limpiar();
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

    private void limpiar() {
        editTextId.setText("");
        editTextNombre.setText("");
        editTextEmail.setText("");
        imageView.setImageResource(R.drawable.ic_menu_camera);
        editTextCosto.setText("");
        editTextTelefono.setText("");
        textViewLatitud.setText("");
        textViewLongitud.setText("");
        mMap.clear();

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


    public void cargarImagenEnImageView(String url, ImageView imageView) {
        if (getContext() != null) {
            Glide.with(getContext())
                    .load(url)  // Aquí va el URL de la imagen
                    .into(imageView);  // Cargamos la imagen en el ImageView
        } else {
            Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

}