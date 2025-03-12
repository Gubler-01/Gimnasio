package com.example.gimnasio.ui.editar;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import com.example.gimnasio.R;
import com.example.gimnasio.db.FirebaseGimnasios;
import com.example.gimnasio.db.Gimnasios;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class EditarFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnMapReadyCallback {

    private EditarViewModel mViewModel;
    private EditText editTextId, editTextNombre, editTextEmail, editTextTelefono, editTextDate, editTextCosto;
    private ImageView imageViewFoto;
    private TextView textViewLatitud, textViewLongitud;
    private VideoView videoView;
    private ImageButton imageButtonCalendario;
    private Button btnLimpiar, btnEditar, btnFoto, btnVideo, btnBuscar;
    private FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri videoUri;

    public static EditarFragment newInstance() {
        return new EditarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editar, container, false);
        firebaseGimnasios = new FirebaseGimnasios(getContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Error: MapFragment no encontrado", Toast.LENGTH_LONG).show();
        }
        componentes(root);
        return root;
    }

    private void componentes(View root) {
        editTextComponentes(root);
        botonComponentes(root);
    }

    private void editTextComponentes(View root) {
        imageButtonCalendario = root.findViewById(R.id.fechaSelectEditar);
        editTextId = root.findViewById(R.id.idEditar);
        editTextNombre = root.findViewById(R.id.nombreEditar);
        editTextEmail = root.findViewById(R.id.emailEditar);
        editTextDate = root.findViewById(R.id.fechaEditar);
        editTextTelefono = root.findViewById(R.id.telefonoEditar);
        editTextCosto = root.findViewById(R.id.costoEditar);
        textViewLatitud = root.findViewById(R.id.latitudEditar);
        textViewLongitud = root.findViewById(R.id.longitudEditar);
    }

    private void botonComponentes(View root) {
        btnLimpiar = root.findViewById(R.id.btnLimpiarEditar);
        btnEditar = root.findViewById(R.id.btnEditar);
        imageViewFoto = root.findViewById(R.id.imagenEditar);
        videoView = root.findViewById(R.id.videoViewEditar);
        btnFoto = root.findViewById(R.id.buttonSelectImageEditar);
        btnVideo = root.findViewById(R.id.buttonSelectVideoEditar);
        btnBuscar = root.findViewById(R.id.btnEditarBuscar);
        btnBuscar.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        btnEditar.setOnClickListener(this);
        btnFoto.setOnClickListener(this);
        imageButtonCalendario.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditarViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLimpiarEditar) {
            limpiar();
        } else if (view.getId() == R.id.btnEditar) {
            if (editTextNombre.getText().toString().isEmpty() ||
                    editTextEmail.getText().toString().isEmpty() ||
                    editTextId.getText().toString().isEmpty() ||
                    editTextDate.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Ingrese la información solicitada", Toast.LENGTH_LONG).show();
            } else {
                try {
                    int id = Integer.parseInt(editTextId.getText().toString());
                    firebaseGimnasios.getRegistroGimnasio(id, new FirebaseGimnasios.OnGimnasioFoundListener() {
                        @Override
                        public void onGimnasioFound(Gimnasios gimnasios) {
                            String nombre = editTextNombre.getText().toString();
                            String email = editTextEmail.getText().toString();
                            String fecha = editTextDate.getText().toString();
                            String telefono = editTextTelefono.getText().toString();
                            String latitud = textViewLatitud.getText().toString();
                            String longitud = textViewLongitud.getText().toString();
                            String costo = editTextCosto.getText().toString();

                            // Convertir imagen a Base64
                            String imageBase64 = gimnasios.getFoto(); // Mantener la existente si no se cambia
                            if (imageViewFoto.getDrawable() != null && imageViewFoto.getDrawable() instanceof BitmapDrawable) {
                                Bitmap bitmap = ((BitmapDrawable) imageViewFoto.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] imageData = baos.toByteArray();
                                imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);
                            }

                            // Convertir video a Base64
                            String videoBase64 = gimnasios.getVideo(); // Mantener el existente si no se cambia
                            if (videoUri != null) {
                                try {
                                    InputStream inputStream = getContext().getContentResolver().openInputStream(videoUri);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        baos.write(buffer, 0, bytesRead);
                                    }
                                    byte[] videoData = baos.toByteArray();
                                    videoBase64 = Base64.encodeToString(videoData, Base64.DEFAULT);
                                    inputStream.close();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Error al leer el video: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            // Actualizar en Realtime Database con callback
                            firebaseGimnasios.updateRegistroGimnasio(gimnasios, nombre, email, fecha, costo, imageBase64, videoBase64, telefono, latitud, longitud,
                                    new FirebaseGimnasios.OnRegistroCompleteListener() {
                                        @Override
                                        public void onRegistroComplete(boolean success) {
                                            Toast.makeText(getContext(), "Editado con éxito", Toast.LENGTH_SHORT).show();
                                            limpiar();
                                        }

                                        @Override
                                        public void onRegistroError(Exception e) {
                                            Toast.makeText(getContext(), "Error al editar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }

                        @Override
                        public void onGimnasioNotFound() {
                            Toast.makeText(getContext(), "Gimnasio no encontrado", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "Error al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        } else if (view.getId() == R.id.fechaSelectEditar) {
            Calendar c = Calendar.getInstance();
            int anio = c.get(Calendar.YEAR);
            int mes = c.get(Calendar.MONTH);
            int dia = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getContext(), this, anio, mes, dia).show();
        } else if (view.getId() == R.id.buttonSelectImageEditar) {
            mCaptura(view);
        } else if (view.getId() == R.id.buttonSelectVideoEditar) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else {
                recordVideo();
            }
        } else if (view.getId() == R.id.btnEditarBuscar) {
            try {
                int id = Integer.parseInt(editTextId.getText().toString());
                firebaseGimnasios.getRegistroGimnasio(id, new FirebaseGimnasios.OnGimnasioFoundListener() {
                    @Override
                    public void onGimnasioFound(Gimnasios gimnasios) {
                        editTextNombre.setText(gimnasios.getNombre());
                        editTextEmail.setText(gimnasios.getEmail());
                        editTextDate.setText(gimnasios.getFecha_ingreso());
                        editTextTelefono.setText(gimnasios.getTelefono());
                        textViewLatitud.setText(gimnasios.getLatitud());
                        textViewLongitud.setText(gimnasios.getLongitud());
                        editTextCosto.setText(gimnasios.getCosto());
                        LatLng ubi = new LatLng(Double.parseDouble(gimnasios.getLatitud()), Double.parseDouble(gimnasios.getLongitud()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi, 10));
                        mMap.addMarker(new MarkerOptions().position(ubi).title("Dirección seleccionada"));
                        cargarImagenEnImageView(gimnasios.getFoto());
                        cargarVideoEnVideoView(gimnasios.getVideo());
                    }

                    @Override
                    public void onGimnasioNotFound() {
                        Toast.makeText(getContext(), "Gimnasio no encontrado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Error al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Ingrese un ID válido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void mCaptura(View v) {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewFoto.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
        } else if (resultCode != RESULT_OK) {
            Toast.makeText(getContext(), "Error al capturar la imagen o video", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiar() {
        editTextId.setText("");
        editTextNombre.setText("");
        editTextEmail.setText("");
        editTextDate.setText("");
        editTextTelefono.setText("");
        editTextCosto.setText("");
        textViewLatitud.setText("");
        textViewLongitud.setText("");
        imageViewFoto.setImageResource(R.drawable.ic_menu_camera);
        videoView.stopPlayback();
        videoView.setVideoURI(null);
        videoUri = null;
        mMap.clear();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        editTextDate.setText(day + "/" + (month + 1) + "/" + year);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng toluca = new LatLng(19.2826, -99.6557);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toluca, 15));
        mMap.setOnMapLongClickListener(latLng -> {
            double latitud = latLng.latitude;
            double longitud = latLng.longitude;
            textViewLatitud.setText(String.valueOf(latitud));
            textViewLongitud.setText(String.valueOf(longitud));
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        });
    }

    private void recordVideo() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5); // Límite de 5 segundos
        if (videoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordVideo();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cargarImagenEnImageView(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageViewFoto.setImageBitmap(decodedBitmap);
        } else {
            imageViewFoto.setImageResource(R.drawable.ic_menu_camera);
        }
    }

    private void cargarVideoEnVideoView(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            try {
                // Decodificar Base64 a bytes
                byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                // Crear un archivo temporal para el video
                File tempFile = File.createTempFile("temp_video", ".mp4", getContext().getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(decodedBytes);
                fos.close();
                // Configurar VideoView con el archivo temporal
                videoView.setVideoPath(tempFile.getAbsolutePath());
                videoView.start();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error al cargar el video: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            videoView.setVideoURI(null);
        }
    }
}