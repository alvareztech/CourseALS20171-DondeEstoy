# CourseALS20171 - ¿Dónde Estoy?

Aplicación de Google Places.`

### Google Api Client


```java
private GoogleApiClient googleApiClient;
```

```java
googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Places.GEO_DATA_API)
        .addApi(Places.PLACE_DETECTION_API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
```
En `onStart`

```java
googleApiClient.connect();
```

En `onStop`

```java
if (googleApiClient.isConnected()) {
    googleApiClient.disconnect();
}
```

### Método obtenerLugaresPosiblesDondeEstoy

```java
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
```

### Método obtener foto

```java
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
```