package com.example.gimnasio.ui.listaractivos;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gimnasio.R;
import com.example.gimnasio.db.FirebaseGimnasios;
import com.example.gimnasio.db.Gimnasios;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ListarActivosFragment extends Fragment {

    private ListarActivosViewModel mViewModel;
    private ArrayList<String> registros ;
    private ArrayList<String> idS;
    public FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;

    public static ListarActivosFragment newInstance() {
        return new ListarActivosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_listar_activos, container, false);
        firebaseGimnasios = new FirebaseGimnasios(getContext());
        firebaseGimnasios.getGimnasiosVigente(new FirebaseGimnasios.OnVig() {
            @Override
            public void onGimnasioFound(ArrayList<String> nombres, ArrayList<String> ids) {
                registros = nombres;
                idS = ids;
                ArrayAdapter<String> adapter = new ArrayAdapter<String> (requireContext(), android.R.layout.simple_list_item_1, registros);
                ListView list = root.findViewById(R.id.listaCuenta);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("MissingInflatedId")
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialogo_gimnasios, null);
                        ((TextView) dialogView.findViewById(R.id.textViewInfoL)).setText(registros.get(i));
                        long id = Integer.parseInt(idS.get(i));


                        firebaseGimnasios.getRegistroGimnasio(Integer.parseInt(String.valueOf(id)), new FirebaseGimnasios.OnGimnasioFoundListener() {
                            @Override
                            public void onGimnasioFound(Gimnasios gimnasios) {
                                ImageView imagen = dialogView.findViewById(R.id.imageViewListaLibro);
                                cargarImagenEnImageView(gimnasios.getFoto(), imagen);
                                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                                getChildFragmentManager().beginTransaction()
                                        .replace(R.id.id_mapDialogo, mapFragment)
                                        .commit();
                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        mMap = googleMap;
                                        LatLng ubi = new LatLng(Double.parseDouble(gimnasios.getLatitud()), Double.parseDouble(gimnasios.getLongitud()));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi, 10));
                                        mMap.clear();
                                        mMap.addMarker(new MarkerOptions().position(ubi).title("Dirección seleccionada"));
                                    }
                                });

                                AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
                                dialogo.setTitle("Gigmnasio");
                                dialogo.setView(dialogView);
                                dialogo.setPositiveButton("Aceptar", null);
                                dialogo.show();
                            }

                            @Override
                            public void onGimnasioNotFound() {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

                    }
                });
            }

            @Override
            public void onGimnasioNotFound() {

            }

            @Override
            public void onError(Exception e) {

            }
        });

        return root;
    }
    public void cargarImagenEnImageView(String url, ImageView imageView) {
        if (getContext() != null) {
            Glide.with(getContext())
                    .load(url)
                    .into(imageView);
        } else {
            Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ListarActivosViewModel.class);
        // TODO: Use the ViewModel
    }

}


