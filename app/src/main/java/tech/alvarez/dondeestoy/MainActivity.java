package tech.alvarez.dondeestoy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

import tech.alvarez.dondeestoy.adapters.LugaresAdapter;
import tech.alvarez.dondeestoy.model.Lugar;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private ImageView fotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fotoImageView = (ImageView) findViewById(R.id.fotoImageView);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            obtenerLugaresPosiblesDondeEstoy();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Activa manualmente los permisos", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 777);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 777) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerLugaresPosiblesDondeEstoy();
            } else {
                Toast.makeText(this, "No diste permiso, no puedo hacer nada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    public void actualizar(View view) {
        obtenerLugaresPosiblesDondeEstoy();
    }

    public void obtenerLugaresPosiblesDondeEstoy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {

                if (placeLikelihoods.getStatus().isSuccess()) {

                    ArrayList<Lugar> datos = new ArrayList<Lugar>();

                    for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                        Place place = placeLikelihood.getPlace();

                        String id = place.getId();
                        String nombre = place.getName().toString();
                        String direccion = place.getAddress().toString();
                        double latitud = place.getLatLng().latitude;
                        double longitud = place.getLatLng().longitude;
                        float probabilidad = placeLikelihood.getLikelihood();
                        float calificacion = place.getRating();

                        datos.add(new Lugar(id, nombre, direccion, latitud, longitud, probabilidad, calificacion));
                    }
                    placeLikelihoods.release();

                    if (datos.size() > 0) { // si hay al menos un lugar obtenemos una foto del primero
                        obtenerFoto(datos.get(0).getId());
                    }

                    LugaresAdapter lugaresAdapter = new LugaresAdapter(MainActivity.this, datos);
                    ListView lugareslistView = (ListView) findViewById(R.id.listView);
                    lugareslistView.setAdapter(lugaresAdapter);

                } else {
                    Toast.makeText(MainActivity.this, "Error obtener lugares: " + placeLikelihoods.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void obtenerFoto(String idLugar) {

        Places.GeoDataApi.getPlacePhotos(googleApiClient, idLugar).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoMetadataResult photos) {

                if (photos.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0) {
                        photoMetadataBuffer.get(0).getScaledPhoto(googleApiClient, fotoImageView.getWidth(), fotoImageView.getHeight())
                                .setResultCallback(new ResultCallback<PlacePhotoResult>() {
                                    @Override
                                    public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                                        if (placePhotoResult.getStatus().isSuccess()) {
                                            fotoImageView.setImageBitmap(placePhotoResult.getBitmap());
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error obtener foto: " + placePhotoResult.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "El lugar no tiene fotos", Toast.LENGTH_SHORT).show();
                    }
                    photoMetadataBuffer.release();
                } else {
                    Toast.makeText(MainActivity.this, "Error encontrar fotos de lugar: " + photos.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
