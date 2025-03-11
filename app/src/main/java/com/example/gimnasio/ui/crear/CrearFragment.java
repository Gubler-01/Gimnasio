package com.example.gimnasio.ui.crear;

import static android.app.Activity.RESULT_OK;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import android.Manifest;

import com.example.gimnasio.LogIn;
import com.example.gimnasio.R;
import com.example.gimnasio.databinding.FragmentCrearBinding;
import com.example.gimnasio.db.FirebaseGimnasios;
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

public class CrearFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private CrearViewModel mViewModel;
    private EditText editTextId, editTextNombre, editTextEmail,editTextTelefono, editTextDate,editTextCosto;
    private ImageView imageViewFoto;
    private TextView textViewLatitud,textViewLongitud;
    private VideoView videoView;
    private ImageButton imageButtonCalendario;
    private Button btnLimpiar, btnCrear,btnFoto,btnVideo;
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
    private int id;
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
            // Maneja el caso donde mapFragment es null
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
        imageViewFoto.setOnClickListener(this);
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
        binding = null;
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
                    editTextDate.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Ingrese la información solicitada", Toast.LENGTH_LONG).show();
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
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();

                    // Subir la imagen
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

                                        // Llama al método para guardar URLs en Firestore
                                        firebaseGimnasios.addRegistroGimnasio(id, nombre, email, fecha, costo, imageUrl, videoUrl, telefono, latitud, longitud, LogIn.usr.getId());
                                        guardarUrlEnFirestore(imageUrl, videoUrl);

                                        Toast.makeText(getContext(), "Agregado con exito", Toast.LENGTH_SHORT).show();
                                        limpiar();
                                    });
                                }).addOnFailureListener(exception -> {
                                    Toast.makeText(getContext(), "Error al subir el video: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(getContext(), "Error al subir la imagen: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });




                }catch (Exception e){
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                    Log.d("exception",e.toString());
                }
            }
        } else if (view.getId() == R.id.fechaSelectCrear) {
            c = Calendar.getInstance();
            anio = c.get(Calendar.YEAR);
            mes = c.get(Calendar.MONTH);
            dia = c.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(getContext(), this, anio, mes, dia);
            datePickerDialog.show();
        } else if (view.getId() == R.id.buttonSelectImageCrear) {
            mCaptura(view);

        } else if (view.getId() == R.id.buttonSelectVideoCrear) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else {
                recordVideo();
            }
        }

    }

    static  final int REQUEST_IMAGE_CAPTURE =1;
    @SuppressLint("QueryPermissionsNeeded")
    @Deprecated
    public void mCaptura(View v){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getContext().getPackageManager())!= null){
            startActivityForResult(pictureIntent,REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewFoto.setImageBitmap(imageBitmap);
            try {
                createImageFile();
                galleryAddpic();
            }catch(Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getContext(), "Fallo del Activity", Toast.LENGTH_LONG).show();

            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
        }else if (resultCode != RESULT_OK) {
            Toast.makeText(getContext(), "Error al capturar la imagen o video", Toast.LENGTH_SHORT).show();
        }


    }

    String photoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "REPA_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFilename, ".jpg", storageDir);
        photoPath = image.getAbsolutePath();
        Toast.makeText(getContext(), photoPath, Toast.LENGTH_LONG).show();
        return image;
    }

    private void galleryAddpic(){
        Intent mediaIntent= new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File savef = new File(photoPath);
        Uri content = Uri.fromFile(savef);
        mediaIntent.setData(content);
        getContext().sendBroadcast(mediaIntent);
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
        editTextDate.setText("");
        editTextEmail.setText("");
        editTextCosto.setText("");
        editTextTelefono.setText("");
        imageViewFoto.setImageResource(R.drawable.ic_menu_camera);
        videoView.stopPlayback();
        videoView.setVideoURI(null);
        mMap.clear();
        textViewLatitud.setText("");
        textViewLongitud.setText("");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        editTextDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
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
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
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




}