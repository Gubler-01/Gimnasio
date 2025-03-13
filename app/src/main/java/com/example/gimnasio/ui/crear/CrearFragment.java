package com.example.gimnasio.ui.crear;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gimnasio.LogIn;
import com.example.gimnasio.R;
import com.example.gimnasio.db.FirebaseGimnasios;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class CrearFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnMapReadyCallback {

    private CrearViewModel mViewModel;
    private EditText editTextId, editTextNombre, editTextEmail, editTextTelefono, editTextDate, editTextCosto;
    private ImageView imageViewFoto;
    private TextView textViewLatitud, textViewLongitud;
    private VideoView videoView;
    private ImageButton imageButtonCalendario;
    private Button btnLimpiar, btnCrear, btnFoto, btnVideo;
    private FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri videoUri;

    public static CrearFragment newInstance() {
        return new CrearFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear, container, false);
        firebaseGimnasios = new FirebaseGimnasios(getContext());
        componentes(root);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Error: MapFragment no encontrado", Toast.LENGTH_LONG).show();
            Log.e("CrearFragment", "El fragmento de mapa es null");
        }
        return root;
    }

    private void componentes(View root) {
        editTextComponentes(root);
        botonComponentes(root);
    }

    private void editTextComponentes(View root) {
        imageButtonCalendario = root.findViewById(R.id.fechaSelectCrear);
        editTextId = root.findViewById(R.id.idCrear);
        editTextNombre = root.findViewById(R.id.nombreCrear);
        editTextEmail = root.findViewById(R.id.emailCrear);
        editTextDate = root.findViewById(R.id.fechaCrear);
        editTextTelefono = root.findViewById(R.id.telefonoCrear);
        editTextCosto = root.findViewById(R.id.costoCrear);
        textViewLatitud = root.findViewById(R.id.latitudCrear);
        textViewLongitud = root.findViewById(R.id.longitudCrear);
    }

    private void botonComponentes(View root) {
        btnLimpiar = root.findViewById(R.id.btnLimpiarCrear);
        btnCrear = root.findViewById(R.id.btnCrear);
        imageViewFoto = root.findViewById(R.id.imagenCrear);
        videoView = root.findViewById(R.id.videoViewCrear);
        btnFoto = root.findViewById(R.id.buttonSelectImageCrear);
        btnVideo = root.findViewById(R.id.buttonSelectVideoCrear);
        btnVideo.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        btnCrear.setOnClickListener(this);
        btnFoto.setOnClickListener(this);
        imageButtonCalendario.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CrearViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLimpiarCrear) {
            limpiar();
        } else if (view.getId() == R.id.btnCrear) {
            if (editTextNombre.getText().toString().isEmpty() ||
                    editTextEmail.getText().toString().isEmpty() ||
                    editTextId.getText().toString().isEmpty() ||
                    editTextDate.getText().toString().isEmpty() ||
                    textViewLatitud.getText().toString().isEmpty() ||
                    textViewLongitud.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Ingrese toda la información solicitada, incluyendo la ubicación", Toast.LENGTH_LONG).show();
            } else {
                try {
                    int id = Integer.parseInt(editTextId.getText().toString());
                    String nombre = editTextNombre.getText().toString();
                    String email = editTextEmail.getText().toString();
                    String fecha = editTextDate.getText().toString();
                    String telefono = editTextTelefono.getText().toString();
                    String latitud = textViewLatitud.getText().toString();
                    String longitud = textViewLongitud.getText().toString();
                    String costo = editTextCosto.getText().toString();

                    // Convertir imagen a Base64
                    String imageBase64 = "";
                    if (imageViewFoto.getDrawable() != null && imageViewFoto.getDrawable() instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) imageViewFoto.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compresión al 50%
                        byte[] imageData = baos.toByteArray();
                        imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);
                        Log.d("CrearFragment", "Tamaño de imagen Base64: " + imageBase64.length() + " caracteres");
                    }

                    // Convertir video a Base64
                    final String[] videoBase64 = {""}; // Usar un array para que sea "efectivamente final"
                    if (videoUri != null) {
                        try {
                            InputStream inputStream = getContext().getContentResolver().openInputStream(videoUri);
                            if (inputStream != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    baos.write(buffer, 0, bytesRead);
                                }
                                inputStream.close();
                                byte[] videoData = baos.toByteArray();
                                videoBase64[0] = Base64.encodeToString(videoData, Base64.DEFAULT);
                                Log.d("CrearFragment", "Tamaño de video Base64: " + videoBase64[0].length() + " caracteres");
                                // Verificar tamaño (aproximado en MB)
                                double videoSizeMB = (videoBase64[0].length() * 0.75) / (1024 * 1024); // Base64 aumenta ~33%, ajustamos
                                if (videoSizeMB > 10) {
                                    Toast.makeText(getContext(), "El video es demasiado grande (" + String.format("%.2f", videoSizeMB) + " MB). Use un video más corto (máx. 3 segundos).", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            } else {
                                Log.w("CrearFragment", "InputStream de videoUri es null");
                                Toast.makeText(getContext(), "Error: No se pudo leer el video", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error al procesar el video: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("CrearFragment", "Error al procesar video: ", e);
                            return;
                        }
                    }

                    // Guardar en Realtime Database con callback
                    firebaseGimnasios.addRegistroGimnasio(id, nombre, email, fecha, costo, imageBase64, videoBase64[0], telefono, latitud, longitud, LogIn.usr.getId(),
                            new FirebaseGimnasios.OnRegistroCompleteListener() {
                                @Override
                                public void onRegistroComplete(boolean success) {
                                    if (success) {
                                        Toast.makeText(getContext(), "Agregado con éxito", Toast.LENGTH_SHORT).show();
                                        Log.d("CrearFragment", "Registro guardado con éxito, incluyendo video: " + (videoBase64[0].isEmpty() ? "sin video" : "con video"));
                                        limpiar();
                                    } else {
                                        Toast.makeText(getContext(), "Error: Registro no completado", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onRegistroError(Exception e) {
                                    Toast.makeText(getContext(), "Error al agregar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e("CrearFragment", "Error al guardar en Firebase: ", e);
                                }
                            });

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    Log.e("CrearFragment", "Error general: ", e);
                }
            }
        } else if (view.getId() == R.id.fechaSelectCrear) {
            Calendar c = Calendar.getInstance();
            int anio = c.get(Calendar.YEAR);
            int mes = c.get(Calendar.MONTH);
            int dia = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getContext(), this, anio, mes, dia).show();
        } else if (view.getId() == R.id.buttonSelectImageCrear) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else {
                mCaptura(view);
            }
        } else if (view.getId() == R.id.buttonSelectVideoCrear) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else {
                recordVideo();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void mCaptura(View v) {
        try {
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (pictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(getContext(), "No se puede abrir la cámara", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("CrearFragment", "Error al iniciar captura de imagen: ", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                if (data != null && data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if (imageBitmap != null) {
                        imageViewFoto.setImageBitmap(imageBitmap);
                        Log.d("CrearFragment", "Imagen capturada correctamente");
                    } else {
                        Toast.makeText(getContext(), "No se pudo obtener la imagen", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: Datos de imagen no disponibles", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CrearFragment", "Error al procesar imagen: ", e);
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            try {
                if (data != null) {
                    videoUri = data.getData();
                    if (videoUri != null) {
                        videoView.setVideoURI(videoUri);
                        videoView.start();
                        Log.d("CrearFragment", "Video capturado: " + videoUri.toString());
                    } else {
                        Toast.makeText(getContext(), "No se pudo obtener el video", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: Datos de video no disponibles", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al procesar el video: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CrearFragment", "Error al procesar video: ", e);
            }
        } else if (resultCode != RESULT_OK) {
            Toast.makeText(getContext(), "Error al capturar la imagen o video", Toast.LENGTH_SHORT).show();
            Log.e("CrearFragment", "onActivityResult: Resultado no OK, código: " + resultCode);
        }
    }

    private void limpiar() {
        editTextId.setText("");
        editTextNombre.setText("");
        editTextDate.setText("");
        editTextEmail.setText("");
        editTextCosto.setText("");
        editTextTelefono.setText("");
        imageViewFoto.setImageResource(R.drawable.ic_menu_camera);
        videoView.stopPlayback();
        videoView.setVideoURI(null);
        videoView.invalidate(); // Forzar actualización de la vista
        mMap.clear();
        textViewLatitud.setText("");
        textViewLongitud.setText("");
        videoUri = null; // Asegurar que se restablezca correctamente
        Log.d("CrearFragment", "Limpieza completada, videoUri: " + (videoUri == null ? "null" : videoUri.toString()));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        editTextDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);

        LatLng toluca = new LatLng(19.2826, -99.6557);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toluca, 15));
        mMap.setOnMapLongClickListener(latLng -> {
            double latitud = latLng.latitude;
            double longitud = latLng.longitude;
            textViewLatitud.setText(String.valueOf(latitud));
            textViewLongitud.setText(String.valueOf(longitud));
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            Log.d("MapClick", "Latitud: " + latitud + ", Longitud: " + longitud);
        });
    }

    private void recordVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3); // Reducir a 3 segundos
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // Baja calidad para reducir tamaño
        if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No se puede abrir la cámara para video", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}