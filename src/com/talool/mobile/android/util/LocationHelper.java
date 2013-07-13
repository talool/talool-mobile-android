package com.talool.mobile.android.util;

import android.location.Location;

public class LocationHelper {
	public float milesBetweenTwoLocations(Location l1, Location l2)
	{
		return l1.distanceTo(l2);
	}
}
