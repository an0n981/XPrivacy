package biz.bokhorst.xprivacy;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;
import android.util.Log;

public class XConnectionCallbacks extends XHook {
	private Methods mMethod;
	private String mClassName;

	private XConnectionCallbacks(Methods method, String restrictionName, String className) {
		super(restrictionName, method.name(), "GMS5." + method.name());
		mMethod = method;
		mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	// @formatter:off

	// abstract void onConnected(Bundle connectionHint)
	// https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks.html
	
	// @formatter:on

	private enum Methods {
		onConnected
	};

	public static List<XHook> getInstances(Object instance) {
		String className = instance.getClass().getName();
		Util.log(null, Log.WARN, "Hooking class=" + className + " uid=" + Binder.getCallingUid());

		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XConnectionCallbacks(Methods.onConnected, null, className));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		switch (mMethod) {
		case onConnected:
			Util.log(this, Log.INFO, "onConnected uid=" + Binder.getCallingUid());
			ClassLoader loader = param.thisObject.getClass().getClassLoader();

			// FusedLocationApi
			Class<?> cLoc = Class.forName("com.google.android.gms.location.LocationServices", false, loader);
			Object fusedLocationApi = cLoc.getDeclaredField("FusedLocationApi").get(null);
			if (PrivacyManager.getTransient(fusedLocationApi.getClass().getName(), null) == null) {
				PrivacyManager.setTransient(fusedLocationApi.getClass().getName(), Boolean.toString(true));

				XPrivacy.hookAll(XFusedLocationApi.getInstances(fusedLocationApi), loader, getSecret());
			}

			// ActivityRecognitionApi
			Class<?> cRec = Class.forName("com.google.android.gms.location.ActivityRecognition", false, loader);
			Object activityRecognitionApi = cRec.getDeclaredField("ActivityRecognitionApi").get(null);
			if (PrivacyManager.getTransient(activityRecognitionApi.getClass().getName(), null) == null) {
				PrivacyManager.setTransient(activityRecognitionApi.getClass().getName(), Boolean.toString(true));
			}

			// AppIndexApi
			Class<?> cApp = Class.forName("com.google.android.gms.appindexing.AppIndex", false, loader);
			Object appIndexApi = cApp.getDeclaredField("AppIndexApi").get(null);
			if (PrivacyManager.getTransient(appIndexApi.getClass().getName(), null) == null) {
				PrivacyManager.setTransient(appIndexApi.getClass().getName(), Boolean.toString(true));
			}

			break;
		}
	}

	@Override
	protected void after(XParam param) throws Throwable {
		// Do nothing
	}
}
