package com.example.new_login;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;

interface onMapReady {
    void onMapReady(@NonNull MapboxMap mapboxMap);

    @SuppressWarnings( {"MissingPermission"})
    void onLocationComponentClick();

    void onCameraTrackingDismissed();

    void onCameraTrackingChanged(int currentMode);

    void onExplanationNeeded(List<String> permissionsToExplain);

    void onPermissionResult(boolean granted);
}
