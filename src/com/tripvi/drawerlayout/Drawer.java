package com.tripvi.drawerlayout;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIFragment;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.WindowProxy;
import android.content.res.Resources;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class Drawer extends TiUIView {

	private DrawerLayout layout;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrowDrawable;

	private RecyclerView menu; /* left drawer */
	private FrameLayout filter; /* right drawer */
	private int menuWidth;
	private int filterWidth;
	private boolean hasMenu = false;
	private boolean hasFilter = false;
	private boolean useCustomDrawer = false;
	private boolean hasToggle = true;
	private int drawable_custom_drawer;
	private boolean useArrowAnimationDrawer = false;
	private boolean useArrowAnimationDrawerCustomColor = false;

	private TiViewProxy leftView;
	private TiViewProxy rightView;
	private TiViewProxy centerView;

	// Static Properties
	public static final String PROPERTY_LEFT_VIEW = "leftView";
	public static final String PROPERTY_CENTER_VIEW = "centerView";
	public static final String PROPERTY_RIGHT_VIEW = "rightView";
	public static final String PROPERTY_LEFT_VIEW_WIDTH = "leftDrawerWidth";
	public static final String PROPERTY_RIGHT_VIEW_WIDTH = "rightDrawerWidth";
	public static final String PROPERTY_DRAWER_INDICATOR_ENABLED = "drawerIndicatorEnabled";
	public static final String PROPERTY_DRAWER_INDICATOR_IMAGE = "drawerIndicatorImage";
	public static final String PROPERTY_DRAWER_LOCK_MODE = "drawerLockMode";
	public static final String PROPERTY_DRAWER_ARROW_ICON = "drawerArrowIcon";
	public static final String PROPERTY_DRAWER_ARROW_ICON_COLOR = "drawerArrowIconColor";

	private static final String TAG = "TripviDrawer";

	int drawable_ic_drawer = 0;
	int string_drawer_open = 0;
	int string_drawer_close = 0;
	int layout_drawer_main = 0;
	int id_content_frame = 0;
	int arrowAnimationDrawerCustomColor = 0;

	public Drawer(final DrawerProxy proxy) {
		super(proxy);

		try {
			drawable_ic_drawer = TiRHelper.getResource("drawable.ic_drawer");
			string_drawer_open = TiRHelper.getResource("string.drawer_open");
			string_drawer_close = TiRHelper.getResource("string.drawer_close");
			layout_drawer_main = TiRHelper.getResource("layout.drawer_main");
			id_content_frame = TiRHelper.getResource("id.content_frame");
		} catch (ResourceNotFoundException e) {
			Log.e(TAG, "XML resources could not be found!!!");
		}

		ActionBarActivity activity = (ActionBarActivity) proxy.getActivity();

		// DrawerLayout을 생성한다.
		LayoutInflater inflater = LayoutInflater.from(activity);
		layout = (DrawerLayout) inflater.inflate(layout_drawer_main, null,
				false);

		layout.setDrawerListener(new DrawerListener());

		// TiUIView
		setNativeView(layout);

	}

	private class DrawerListener implements DrawerLayout.DrawerListener {

		@Override
		public void onDrawerClosed(View drawerView) {
			if (proxy.hasListeners("drawerclose")) {
				KrollDict options = new KrollDict();
				if (drawerView.equals(menu)) {
					options.put("drawer", "left");
				} else if (drawerView.equals(filter)) {
					options.put("drawer", "right");
				}
				proxy.fireEvent("drawerclose", options);
			}
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			if (proxy.hasListeners("draweropen")) {
				KrollDict options = new KrollDict();
				if (drawerView.equals(menu)) {
					options.put("drawer", "left");
				} else if (drawerView.equals(filter)) {
					options.put("drawer", "right");
				}
				proxy.fireEvent("draweropen", options);
			}
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			if (proxy.hasListeners("drawerslide")) {
				KrollDict options = new KrollDict();
				options.put("offset", slideOffset);
				if (drawerView.equals(menu)) {
					options.put("drawer", "left");
				} else if (drawerView.equals(filter)) {
					options.put("drawer", "right");
				}
				proxy.fireEvent("drawerslide", options);
			}

			if (useArrowAnimationDrawer && drawerView.equals(menu)) {
				// Sometimes slideOffset ends up so close to but not quite 1 or
				// 0.
				if (slideOffset >= .995) {
					drawerArrowDrawable.setFlip(true);
				} else if (slideOffset <= .005) {
					drawerArrowDrawable.setFlip(false);
				}
				drawerArrowDrawable.setParameter(slideOffset);
			}
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			if (proxy.hasListeners("change")) {
				KrollDict options = new KrollDict();
				options.put("state", newState);
				options.put("idle", (newState == 0 ? 1 : 0));
				options.put("dragging", (newState == 1 ? 1 : 0));
				options.put("settling", (newState == 2 ? 1 : 0));
				proxy.fireEvent("change", options);
			}
		}
	}

	/**
	 * Open/Close/Toggle drawers
	 */
	public void toggleLeftDrawer() {
		if (layout.isDrawerOpen(Gravity.START)) {
			closeLeftDrawer();
		} else {
			openLeftDrawer();
		}
	}

	public void openLeftDrawer() {
		layout.openDrawer(Gravity.START);
	}

	public void closeLeftDrawer() {
		layout.closeDrawer(Gravity.START);
	}

	public void toggleRightDrawer() {
		if (layout.isDrawerOpen(Gravity.END)) {
			closeRightDrawer();
		} else {
			openRightDrawer();
		}
	}

	public void openRightDrawer() {
		layout.openDrawer(Gravity.END);
	}

	public void closeRightDrawer() {
		layout.closeDrawer(Gravity.END);
	}

	public boolean isLeftDrawerOpen() {
		return layout.isDrawerOpen(Gravity.START);
	}

	public boolean isRightDrawerOpen() {
		return layout.isDrawerOpen(Gravity.END);
	}

	public boolean isLeftDrawerVisible() {
		return layout.isDrawerVisible(Gravity.START);
	}

	public boolean isRightDrawerVisible() {
		return layout.isDrawerVisible(Gravity.END);
	}

	private void initDrawerToggle() {

		ActionBarActivity activity = (ActionBarActivity) proxy.getActivity();

		if (activity.getSupportActionBar() == null) {
			return;
		}
		
		// enable ActionBar app icon to behave as action to toggle nav
		// drawer
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);

		int drawer_drawable;

		if (useCustomDrawer) {
			drawer_drawable = drawable_custom_drawer;
		} else {
			drawer_drawable = drawable_ic_drawer;
		}
		if (useArrowAnimationDrawer) {
			Resources resources = activity.getResources();
			drawerArrowDrawable = new DrawerArrowDrawable(resources);
			if (useArrowAnimationDrawerCustomColor) {
				drawerArrowDrawable
						.setStrokeColor(arrowAnimationDrawerCustomColor);
			}

			activity.getSupportActionBar().setHomeAsUpIndicator(
					drawerArrowDrawable);
		} else {

			// ActionBarDrawerToggle ties together the the proper interactions
			// between the sliding drawer and the action bar app icon
			mDrawerToggle = new ActionBarDrawerToggle(activity, layout,
					drawer_drawable, string_drawer_open, string_drawer_close) {
				@Override
				public void onDrawerClosed(View drawerView) {
					super.onDrawerClosed(drawerView);
					if (proxy.hasListeners("drawerclose")) {
						KrollDict options = new KrollDict();
						if (drawerView.equals(menu)) {
							options.put("drawer", "left");
						} else if (drawerView.equals(filter)) {
							options.put("drawer", "right");
						}
						proxy.fireEvent("drawerclose", options);
					}
				}

				@Override
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					if (proxy.hasListeners("draweropen")) {
						KrollDict options = new KrollDict();
						if (drawerView.equals(menu)) {
							options.put("drawer", "left");
						} else if (drawerView.equals(filter)) {
							options.put("drawer", "right");
						}
						proxy.fireEvent("draweropen", options);
					}
				}

				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					super.onDrawerSlide(drawerView, slideOffset);
					if (proxy.hasListeners("drawerslide")) {
						KrollDict options = new KrollDict();
						options.put("offset", slideOffset);
						if (drawerView.equals(menu)) {
							options.put("drawer", "left");
						} else if (drawerView.equals(filter)) {
							options.put("drawer", "right");
						}
						proxy.fireEvent("drawerslide", options);
					}
				}

				@Override
				public void onDrawerStateChanged(int newState) {
					super.onDrawerStateChanged(newState);

					if (proxy.hasListeners("change")) {
						KrollDict options = new KrollDict();
						options.put("state", newState);
						options.put("idle", (newState == 0 ? 1 : 0));
						options.put("dragging", (newState == 1 ? 1 : 0));
						options.put("settling", (newState == 2 ? 1 : 0));
						proxy.fireEvent("change", options);
					}
				}
			};
			// Set the drawer toggle as the DrawerListener
			layout.setDrawerListener(mDrawerToggle);

			// onPostCreate 대신에
			layout.post(new Runnable() {
				@Override
				public void run() {
					mDrawerToggle.syncState();
				}
			});
		}
	}

	/**
	 * drawer가 필요할때 그때그때 추가
	 */
	private void initLeftDrawer(View leftView) {
		if (hasMenu) {
			return;
		}

		Log.d(TAG, "initializing left drawer");
		
		menu.setHasFixedSize(true);
		
		// menu: left drawer
		//menu = new FrameLayout(proxy.getActivity());
		LayoutParams menuLayout = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT, Gravity.START);
		menu.setLayoutParams(menuLayout);
		
		LinearLayoutManager menuLayoutManager = new LinearLayoutManager(proxy.getActivity());
		menu.setLayoutManager(menuLayoutManager);
		
		if (leftView != null){
			leftView.setLayoutParams(menuLayout);
			layout.addView(leftView);
		}
		
		//this.menu.addView(leftView);
		//layout.addView(menu);

		hasMenu = true;

		if (hasToggle) {
			initDrawerToggle();
		}
	}

	private void initRightDrawer() {
		if (hasFilter) {
			return;
		}

		Log.d(TAG, "initializing right drawer");

		// filter: right drawer
		filter = new FrameLayout(proxy.getActivity());
		LayoutParams filterLayout = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT);
		filterLayout.gravity = Gravity.END;
		filter.setLayoutParams(filterLayout);

		layout.addView(filter);

		hasFilter = true;
	}

	/**
	 * centerView 변경
	 */
	public void replaceCenterView(TiViewProxy viewProxy, boolean backstack) {
		if (viewProxy == this.centerView) {
			Log.d(TAG, "centerView was not changed");
			return;
		}
		if (viewProxy == null) {
			return;
		}

		String name = viewProxy.getApiName();
		// If view is map, we need to create a standalone fragment. This
		// can be done by set the property here before creating the view
		// or set it when you create the map in Javascript.
		if (name == "Ti.Map") {
			viewProxy.setProperty(TiC.PROPERTY_FRAGMENT_ONLY, true);
		}
		TiUIView contentView = viewProxy.getOrCreateView();
		FragmentManager fragmentManager = ((ActionBarActivity) proxy
				.getActivity()).getSupportFragmentManager();
		// since only map uses TiUIFragment, here we check if view is a map,
		// then we add the fragment directly.
		if (contentView instanceof TiUIFragment) {
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.replace(id_content_frame,
					((TiUIFragment) contentView).getFragment());
			if (backstack) {
				Log.d(TAG, "adding Fragment to backstack");
				ft.addToBackStack(name);
			}
			ft.commit();
		} else {
			View view = contentView.getOuterView();
			ContentWrapperFragment fragment = new ContentWrapperFragment();
			fragment.setContentView(view);
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.replace(id_content_frame, fragment);
			if (backstack) {
				Log.d(TAG, "adding Fragment to backstack");
				ft.addToBackStack(name);
			}
			ft.commit();
		}

		this.centerView = viewProxy;
	}

	@Override
	public void processProperties(KrollDict d) {
		if (d.containsKey(PROPERTY_DRAWER_INDICATOR_IMAGE)) {
			String imageUrl = d.getString(PROPERTY_DRAWER_INDICATOR_IMAGE);
			drawable_custom_drawer = TiUIHelper.getResourceId(proxy.resolveUrl(
					null, imageUrl));
			if (drawable_custom_drawer != 0) {
				useCustomDrawer = true;
			}
		}
		if (d.containsKey(PROPERTY_DRAWER_INDICATOR_ENABLED)) {
			hasToggle = TiConvert.toBoolean(d,
					PROPERTY_DRAWER_INDICATOR_ENABLED);
		}

		if (d.containsKey(PROPERTY_DRAWER_ARROW_ICON)) {
			useArrowAnimationDrawer = TiConvert.toBoolean(d
					.get(PROPERTY_DRAWER_ARROW_ICON));
			if (d.containsKey(PROPERTY_DRAWER_ARROW_ICON_COLOR)) {
				useArrowAnimationDrawerCustomColor = true;
				arrowAnimationDrawerCustomColor = TiConvert.toColor(d
						.getString(PROPERTY_DRAWER_ARROW_ICON_COLOR));
			}
		}

		if (d.containsKey(PROPERTY_LEFT_VIEW)) {
			Object leftView = d.get(PROPERTY_LEFT_VIEW);
			if (leftView != null && leftView instanceof TiViewProxy) {
				if (leftView instanceof WindowProxy)
					throw new IllegalStateException(
							"[ERROR] Cannot add window as a child view of other window");
				this.leftView = (TiViewProxy) leftView;
				View nativeLeftView = getNativeView(this.leftView);
				if (nativeLeftView != null){
					this.initLeftDrawer(nativeLeftView);
				}
			} else {
				Log.e(TAG, "[ERROR] Invalid type for leftView");
			}
		}
		if (d.containsKey(PROPERTY_RIGHT_VIEW)) {
			Object rightView = d.get(PROPERTY_RIGHT_VIEW);
			if (rightView != null && rightView instanceof TiViewProxy) {
				if (rightView instanceof WindowProxy)
					throw new IllegalStateException(
							"[ERROR] Cannot add window as a child view of other window");
				this.rightView = (TiViewProxy) rightView;
				this.initRightDrawer();
				this.filter.addView(getNativeView(this.rightView));
			} else {
				Log.e(TAG, "[ERROR] Invalid type for rightView");
			}
		}
		if (d.containsKey(PROPERTY_CENTER_VIEW)) {
			Object centerView = d.get(PROPERTY_CENTER_VIEW);
			if (centerView != null && centerView instanceof TiViewProxy) {
				if (centerView instanceof WindowProxy)
					throw new IllegalStateException(
							"[ERROR] Cannot use window as a child view of other window");
				replaceCenterView((TiViewProxy) centerView, false);
			} else {
				Log.e(TAG, "[ERROR] Invalid type for centerView");
			}
		}
		if (d.containsKey(PROPERTY_LEFT_VIEW_WIDTH)) {
			
			menuWidth = getDevicePixels(d.get(PROPERTY_LEFT_VIEW_WIDTH));

			Log.d(TAG, "set menuWidth = " + d.get(PROPERTY_LEFT_VIEW_WIDTH)
					+ " in pixel: " + menuWidth);

			menu.getLayoutParams().width = menuWidth;
		}
		if (d.containsKey(PROPERTY_RIGHT_VIEW_WIDTH)) {
			filterWidth = getDevicePixels(d.get(PROPERTY_RIGHT_VIEW_WIDTH));

			Log.d(TAG, "set filterWidth = " + d.get(PROPERTY_RIGHT_VIEW_WIDTH)
					+ " in pixel: " + filterWidth);

			filter.getLayoutParams().width = filterWidth;
		}
		if (d.containsKey(PROPERTY_DRAWER_LOCK_MODE)) {
			layout.setDrawerLockMode(TiConvert.toInt(d
					.get(PROPERTY_DRAWER_LOCK_MODE)));
		}

		super.processProperties(d);
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {

		Log.d(TAG, "propertyChanged  Property: " + key + " old: " + oldValue
				+ " new: " + newValue);

		if (key.equals(PROPERTY_LEFT_VIEW)) {
			if (newValue == this.leftView)
				return;
			TiViewProxy newProxy = null;
			int index = 0;
			if (this.leftView != null) {
				index = this.menu.indexOfChild(this.leftView.getOrCreateView()
						.getNativeView());
			}
			if (newValue != null && newValue instanceof TiViewProxy) {
				if (newValue instanceof WindowProxy)
					throw new IllegalStateException(
							"[ERROR] Cannot add window as a child view of other window");
				this.leftView = (TiViewProxy) newValue;
				View nativeLeftView = getNativeView(this.leftView);
				this.initLeftDrawer(nativeLeftView);
			} else {
				Log.e(TAG, "[ERROR] Invalid type for leftView");
			}
			if (this.leftView != null) {
				this.menu.removeView(this.leftView.getOrCreateView()
						.getNativeView());
			}
			this.leftView = newProxy;
		} else if (key.equals(PROPERTY_RIGHT_VIEW)) {
			if (newValue == this.rightView)
				return;
			TiViewProxy newProxy = null;
			int index = 0;
			if (this.rightView != null) {
				index = this.filter.indexOfChild(this.rightView
						.getOrCreateView().getNativeView());
			}
			if (newValue != null && newValue instanceof TiViewProxy) {
				if (newValue instanceof WindowProxy)
					throw new IllegalStateException(
							"[ERROR] Cannot add window as a child view of other window");
				newProxy = (TiViewProxy) newValue;
				initRightDrawer();
				this.filter.addView(newProxy.getOrCreateView().getOuterView(),
						index);
			} else {
				Log.e(TAG, "[ERROR] Invalid type for rightView");
			}
			if (this.rightView != null) {
				this.filter.removeView(this.rightView.getOrCreateView()
						.getNativeView());
			}
			this.rightView = newProxy;
		} else if (key.equals(PROPERTY_CENTER_VIEW)) {
			TiViewProxy newProxy = (TiViewProxy) newValue;
			replaceCenterView(newProxy, false);
		} else if (key.equals(PROPERTY_LEFT_VIEW_WIDTH)) {
			menuWidth = getDevicePixels(newValue);

			Log.d(TAG, "change menuWidth = " + newValue + " in pixel: "
					+ menuWidth);

			menu.getLayoutParams().width = menuWidth;
		} else if (key.equals(PROPERTY_RIGHT_VIEW_WIDTH)) {
			filterWidth = getDevicePixels(newValue);

			Log.d(TAG, "change filterWidth = " + newValue + " in pixel: "
					+ filterWidth);

			initRightDrawer();

			LayoutParams filterLayout = new LayoutParams(filterWidth,
					LayoutParams.MATCH_PARENT);
			filterLayout.gravity = Gravity.END;
			this.filter.setLayoutParams(filterLayout);
		} else if (key.equals(PROPERTY_DRAWER_LOCK_MODE)) {
			layout.setDrawerLockMode(TiConvert.toInt(newValue));
		} else if (key.equals(PROPERTY_DRAWER_INDICATOR_ENABLED)) {
			boolean b = (Boolean) newValue;
			mDrawerToggle.setDrawerIndicatorEnabled(b);
		} else if (key.equals(PROPERTY_DRAWER_ARROW_ICON_COLOR)) {
			useArrowAnimationDrawerCustomColor = true;
			String color = (String) newValue;
			arrowAnimationDrawerCustomColor = TiConvert.toColor(color);
			drawerArrowDrawable.setStrokeColor(arrowAnimationDrawerCustomColor);
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}

	/**
	 * Helpers
	 */
	public int getDevicePixels(Object value) {
		return TiConvert.toTiDimension(TiConvert.toString(value),
				TiDimension.TYPE_WIDTH).getAsPixels(layout);
	}

	private View getNativeView(TiViewProxy viewProxy) {
		View nativeView = viewProxy.getOrCreateView().getOuterView();
		ViewGroup parentViewGroup = (ViewGroup) nativeView.getParent();
		if (parentViewGroup != null) {
			parentViewGroup.removeAllViews();
		}
		return nativeView;
	}

}
