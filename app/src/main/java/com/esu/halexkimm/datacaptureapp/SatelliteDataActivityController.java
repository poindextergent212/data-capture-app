package com.esu.halexkimm.datacaptureapp;

/**
 * NOTE: Listening to GPS status and NMEA is very resource intensive. That's why
 * this has it's own dedicated view. Related to this, I've noticed delays when
 * switching back to the main view.
 *
 * @author Andy Gup
 *
 */
public class SatelliteDataActivityController {
    /*private static LocationListener _locationListenerGPSProvider = null;
    private static LocationManager _locationManager = null;
    private static Activity _activity = null;

    private static ElapsedTimer _elapsedTimer;

    private static TextView _gpsSatelliteTextView;
    private TextView _gpsNMEATextView;

    private static SharedPreferences _preferences;

    private ImageView _imMainActivity;

    private static Button _startButton;
    private static Button _pauseButton;

    private static NmeaListener _nmeaListener = null;
    private static GpsStatus.Listener _gpsStatusListener = null;

    private static Iterable<GpsSatellite> _satellites = null;

    private final static String _format = String.format(Locale.getDefault(),"%%0%dd", 2);

    public SatelliteDataActivityController(Activity activity){
        _activity = activity;
        _startButton = (Button)_activity.findViewById(R.id.startButton);
        _pauseButton = (Button) _activity.findViewById(R.id.PauseSatButton);

        setUI();
    }

    private void setUI(){

        _startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(_locationManager == null){
                    startLocation();
                }
                else{
                    stopLocation();
                }
            }
        });

        _pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pauseLocation();
            }
        ;

    }

    public void startLocation(){

        if(_locationManager == null){

            if(_startButton != null){
                _startButton.setTextColor(Color.RED);
                _startButton.setText("Stop");
            }

            _locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    Toast.makeText(_activity.getBaseContext(),
                            "Location changed: Lat: " + location.getLatitude() + " Lng: "
                                    + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    String longitude = "Longitude: " + location.getLongitude();
                    String latitude = "Latitude: " + location.getLatitude();

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Boolean gpsProviderEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(gpsProviderEnabled){
                setLocationListenerGPSProvider();
            }
        }
        else{
            Log.d("GPSTester","GPS Provider not enabled. Unable to set location listener.");
        }
    }

    public void stopLocation(){
        if(_locationManager != null){


            if(_startButton != null){
                _startButton.setTextColor(Color.WHITE);
                _startButton.setText("Start");
            }

            if(_nmeaListener != null){
                _locationManager.removeNmeaListener(_nmeaListener);
                _nmeaListener = null;
            }

            if(_gpsStatusListener != null){
                _locationManager.removeGpsStatusListener(_gpsStatusListener);
                _gpsStatusListener = null;
            }

            if(_locationListenerGPSProvider != null){
                _locationManager.removeUpdates(_locationListenerGPSProvider);
                _locationManager = null;
            }
        }

        if(_locationListenerGPSProvider != null) {
            _locationListenerGPSProvider = null;
        }
    }

    public void pauseLocation(){
        String startButtonText = _startButton.getText().toString();
        if(_locationManager != null && startButtonText == "Stop"){
            _pauseButton.setTextColor(Color.RED);

            if(_locationListenerGPSProvider != null){
                _locationManager.removeUpdates(_locationListenerGPSProvider);
                _locationManager = null;
            }

            if(_locationListenerGPSProvider != null) {
                _locationListenerGPSProvider = null;
            }

            if(_gpsStatusListener != null){
                _gpsStatusListener = null;
            }

            if(_nmeaListener != null){
                _nmeaListener = null;
            }
        }
        else if(_locationManager == null && startButtonText == "Stop"){
            _pauseButton.setTextColor(Color.WHITE);
            //_elapsedTimer.unpauseTimer();
            _locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);

            Boolean gpsProviderEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(gpsProviderEnabled == true && _preferences.getBoolean("pref_key_gps", true) == true){
            //    setLocationListenerGPSProvider();
            }
            else{
                Log.d("GPSTester","GPS Provider not enabled. Unable to set location listener.");
            }

            gpsProviderEnabled = null;
        }
    }*/
}