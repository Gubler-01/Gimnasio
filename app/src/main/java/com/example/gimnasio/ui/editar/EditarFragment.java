package com.example.gimnasio.ui.editar;

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
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditarFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private EditarViewModel mViewModel;
    private EditText editTextId, editTextNombre, editTextEmail, editTextTelefono, editTextDate, editTextCosto;
    private ImageView imageViewFoto;
    private TextView textViewLatitud, textViewLongitud;
    private VideoView videoView;
    private ImageButton imageButtonCalendario;
    private Button btnLimpiar, btnCrear, btnFoto, btnVideo,btnBuscar;
    private FragmentCrearBinding binding;
    private String a = "", d = "", sex = "";
    private Calendar c;
    private static int anio, mes, dia;
    private DatePickerDialog datePickerDialog;
    public FirebaseGimnasios firebaseGimnasios;
    private GoogleMap mMap;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private Uri videoUri;
    private String imageUrl, videoUrl;
    private static final int PERMISSION_REQUEST_CODE = 100;

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
            // Maneja el caso donde mapFragment es null
            Log.e("CrearFragment", "El fragmento de mapa es null");
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
        btnCrear = root.findViewById(R.id.btnEditar);
        imageViewFoto = root.findViewById(R.id.imagenEditar);
        videoView = root.findViewById(R.id.videoViewEditar);
        btnFoto = root.findViewById(R.id.buttonSelectImageEditar);
        btnVideo = root.findViewById(R.id.buttonSelectVideoEditar);
        btnBuscar = root.findViewById(R.id.btnEditarBuscar);
        btnBuscar.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        btnCrear.setOnClickListener(this);
        btnFoto.setOnClickListener(this);
        imageViewFoto.setOnClickListener(this);
        imageButtonCalendario.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditarViewModel.class);

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @SuppressLint("QueryPermissionsNeeded")
    @Deprecated
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
            try {
                createImageFile();
                galleryAddpic();
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getContext(), "Fallo del Activity", Toast.LENGTH_LONG).show();

            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == getActivity().RESULT_OK) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start(); // Reproduce el video automáticamente
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnLimpiarEditar) {

            limpiar();
        } else if (view.getId() == R.id.btnEditar) {
            if (view.getId() == R.id.btnEditar) {
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
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();

                                Bitmap bitmap = ((BitmapDrawable) imageViewFoto.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] imageData = baos.toByteArray();

                                String imagePath = "images/" + UUID.randomUUID().toString() + ".jpg";
                                StorageReference imageRef = storageRef.child(imagePath);

                                UploadTask imageUploadTask = imageRef.putBytes(imageData);
                                imageUploadTask.addOnSuccessListener(taskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                        imageUrl = imageUri.toString();

                                        if (videoUri != null) {
                                            String videoPath = "videos/" + UUID.randomUUID().toString() + ".mp4";
                                            StorageReference videoRef = storageRef.child(videoPath);

                                            UploadTask videoUploadTask = videoRef.putFile(videoUri);
                                            videoUploadTask.addOnSuccessListener(taskSnapshot2 -> {
                                                videoRef.getDownloadUrl().addOnSuccessListener(videoUri2 -> {
                                                    videoUrl = videoUri2.toString();

                                                    firebaseGimnasios.updateRegistroGimnasio(gimnasios, nombre, email, fecha, costo, imageUrl, videoUrl, telefono, latitud, longitud);

                                                    Toast.makeText(getContext(), "Editado con exito", Toast.LENGTH_SHORT).show();
                                                    limpiar();
                                                    guardarUrlEnFirestore(imageUrl, videoUrl);
                                                });
                                            }).addOnFailureListener(exception -> {

                                                Toast.makeText(getContext(), "Error al subir el video: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                        }else {
                                            firebaseGimnasios.updateRegistroGimnasio(gimnasios, nombre, email, fecha, costo, imageUrl, gimnasios.getVideo(), telefono, latitud, longitud);
                                            Toast.makeText(getContext(), "Editado con exito", Toast.LENGTH_SHORT).show();
                                            limpiar();
                                        }
                                    });
                                }).addOnFailureListener(exception -> {
                                    Toast.makeText(getContext(), "Error al subir la imagen: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onGimnasioNotFound() {}

                            @Override
                            public void onError(Exception e) {}
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        Log.d("exception", e.toString());
                    }
                }
            }
        } else if (view.getId() == R.id.fechaSelectCrear) {
            c = Calendar.getInstance();
            anio = c.get(Calendar.YEAR);
            mes = c.get(Calendar.MONTH);
            dia = c.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(getContext(), this, anio, mes, dia);
            datePickerDialog.show();
        } else if (view.getId() == R.id.buttonSelectImageEditar) {
            mCaptura(view);

        } else if (view.getId() == R.id.buttonSelectVideoEditar) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA}, REQUEST_VIDEO_CAPTURE);
            } else {
                recordVideo();
            }

        } else if (view.getId() == R.id.btnEditarBuscar) {
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
                    LatLng ubi = new LatLng(Double.parseDouble(textViewLatitud.getText().toString()), Double.parseDouble(textViewLongitud.getText().toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi, 10));
                    mMap.addMarker(new MarkerOptions().position(ubi).title("Dirección seleccionada"));
                    cargarImagenEnImageView(gimnasios.getFoto(),imageViewFoto);
                    cargarVideoEnVideoView(gimnasios.getVideo());
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

    String photoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddpic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }
    public void guardarUrlEnFirestore(String imagenUrl,String videoUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> registro = new HashMap<>();
        registro.put("imagenUrl", imagenUrl);  // Guardar la URL de descarga
        registro.put("videoUrl", videoUrl);
        db.collection("Gimnasios").add(registro)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Imagen y video guardados exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        videoView.setVideoURI(null);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        editTextDate.setText(String.format("%d-%02d-%02d", year, month + 1, day));
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

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            }
        });


    }

    private void recordVideo() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordVideo();
            }
        }
    }
    private void cargarImagenEnImageView(String url, ImageView imageView) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_menu_camera)
                .error(R.drawable.ic_menu_camera)
                .into(imageView);
    }

    private void cargarVideoEnVideoView(String url) {
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);
        videoView.start();
    }



}