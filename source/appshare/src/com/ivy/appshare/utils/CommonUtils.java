package com.ivy.appshare.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.R;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.LocalSetting.UserIconEnvironment;
import com.ivy.appshare.engin.im.Im.FileType;

public class CommonUtils {
	private static final String TAG = CommonUtils.class.getSimpleName();

	public static final String VIDEO_URI = Video.Media.getContentUri("external").toString();
	public static final String IMAGE_URI = Images.Media.getContentUri("external").toString();
	public static final String AUDIO_URI = Audio.Media.getContentUri("external").toString();
	public static final String MIMETYPE_VCARD        = "text/x-vcard";
	public static final String MIMETYPE_APPLICATION  = "application/vnd.android.package-archive";
	public static final String MIMETYPE_ALLIMAGES  = "image/*";
	public static final String MIMETYPE_ALLVIDEOS  = "video/*";
	public static final String MIMETYPE_ALLAUDIOS  = "audio/*";

	private static final int DISPLAY_WIDTH = 100;
	private static final int DISPLAY_HEIGHT = 100;

	public final static Bitmap DecodeBitmap(String BitmapPath, int maxNumOfPixels) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(BitmapPath, opts);

        opts.inSampleSize = CommonUtils.computeSampleSize(opts, -1, maxNumOfPixels);
        opts.inJustDecodeBounds = false;

        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(BitmapPath, opts);
        } catch (OutOfMemoryError err) {
            Log.i(TAG, "InitPicContent exception! " + err);
            return null;
        }
        return bmp;
	}

	public final static void getPersonPhoto(ImageView imageLeft,String personHeadIcon) {
		LocalSetting mLocalSetting = LocalSetting.getInstance();
		UserIconEnvironment userIconEnvironment = mLocalSetting
				.getUserIconEnvironment();

		if (userIconEnvironment.isExistHead(personHeadIcon, -1)) {
			Bitmap bitmap = CommonUtils.DecodeBitmap(
					userIconEnvironment.getFriendHeadFullPath(personHeadIcon),
					256 * 256);
			imageLeft.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageLeft.setImageBitmap(bitmap);
		} else {
			imageLeft.setImageResource(R.drawable.contact_photo_default);
		}
	}

    private final static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
        // Log.i(TAG, "computeInitialSampleSize");
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
            (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
    public final static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        // Log.i(TAG, "computeSampleSize");
        int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

	public static String getFileNameByPath(String path) {
		if (path == null) {
			return null;
		}
		return path.substring(path.lastIndexOf('/')+1);
	}

	public static String getVCardByUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        String pathName = null;
        OutputStream os = null;
        InputStream is = null;
        if (cursor != null && cursor.getCount() > 0) {
        	cursor.moveToNext();

        	//String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String displayName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));

            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);

            try {
                is = context.getContentResolver().openInputStream(shareUri);
                pathName = LocalSetting.getInstance().getLocalPath()
                    + "/" + displayName + ".vcf";
                os = new FileOutputStream(new File(pathName));
                byte[] buffer = new byte[256];
                for (int len = 0; (len = is.read(buffer)) != -1;) {
                    os.write(buffer, 0, len);
                }
            } catch (FileNotFoundException e) {
            	pathName = null;
            	Log.e(TAG, "getVCardByUri FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
            	pathName = null;
            	Log.e(TAG, "getVCardByUri IOException");
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {

                }
            }
        } else {
        	Log.e(TAG, "getVCardByUri cursor is null");
        	return null;
        }

        return pathName;
	}
	
    @SuppressLint("NewApi")
	public static void viewFile(Context context, FileType fileType, String content) {
		switch (fileType) {
			case FileType_Contact: {
				// view vcard
				try {
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_VCARD);
			        context.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					
				}
				break;
			}
	        case FileType_Picture:
	            // view picture
	            Intent viewPiciItent = new Intent(Intent.ACTION_VIEW);
	            viewPiciItent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_ALLIMAGES);
	            context.startActivity(viewPiciItent);
	            break;
	        case FileType_Music:
	            //play music
	            Intent playMusicItent = new Intent(Intent.ACTION_VIEW);
	            playMusicItent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_ALLAUDIOS);
	            context.startActivity(playMusicItent);
	            break;
	        case FileType_Video:
	            //play video
	            Intent viewVideoItent = new Intent(Intent.ACTION_VIEW);
	            viewVideoItent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_ALLVIDEOS);
	            context.startActivity(viewVideoItent);
	            break;
	        case FileType_App: {
	                Intent intent = new Intent();
	                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
	                    intent.setAction(Intent.ACTION_VIEW);
	                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
	                    intent.putExtra("pkg", content);
	                } else {
	                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
	                    intent.setData(Uri.fromParts("package", content, null));
	                }
	                context.startActivity(intent); 
		        	break;
                }
		}
	}

    public static void shareWithBluttooth(Context context, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(content)));
    	intent.setType("*/*");
    	BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    	if (adapter != null) {
    		intent.setPackage("com.android.bluetooth");
    		context.startActivity(intent);
    	}
    }

	public static void importVcard(Context context, String content) {
        // import vcard
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_VCARD);
        Intent intentDirect = new Intent(intent);
        intentDirect.setComponent(new ComponentName("com.android.contacts","com.android.contacts.vcard.ImportVCardActivity"));
        try{
        	context.startActivity(intentDirect);
        } catch(ActivityNotFoundException e){
            intentDirect.setComponent(new ComponentName("com.android.contacts","com.android.contacts.ImportVCardActivity"));
            try {
            	context.startActivity(intentDirect);
            } catch (ActivityNotFoundException e1){
            	context.startActivity(intent);
            }
        }
	}

	public static void installApp(Context context, String content) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(content)), CommonUtils.MIMETYPE_APPLICATION);
        context.startActivity(intent);
	}

	public static void unInstallApp(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);  
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
	}

	public static void unInstallAppAsync(Context context, String packageName, String sourceDir) {
	    CopyAndUninstallApp instance = new CopyAndUninstallApp(context, packageName, sourceDir);
	    instance.start();
	}

	public static void deleteFile(String filePath) {
	    File file = new File(filePath);
	    boolean a = file.delete();
	}

	public static void launchApp(Context context, String packageName) {
		Intent intent = context.getPackageManager().
				getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
	}

	
    //check the Network is available
    public static boolean isNetworkAvailable() {
        try{
            ConnectivityManager cm = (ConnectivityManager)MyApplication.getInstance()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
            return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("NewApi")
    public static AlertDialog.Builder getMyAlertDialogBuilder(Context context) {
        // if adk version >= 14, use the new theme. postive button on right.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        } else {
            return new AlertDialog.Builder(context);
        }
    }

	public static Bitmap decodeBitmap(String path) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op);
		int wRatio = (int) Math.ceil(op.outWidth / DISPLAY_WIDTH);
		int hRatio = (int) Math.ceil(op.outHeight / DISPLAY_HEIGHT);
		if (wRatio > 1 && hRatio > 1) {
			if (wRatio > hRatio) {
				op.inSampleSize = wRatio;
			} else {
				op.inSampleSize = hRatio;
			}
		}
		op.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(path, op);
		return bmp;
	}
	
	
	private static class CopyAndUninstallApp extends Thread {
	    private Context mContext;
	    private String mPackageName;
	    private String mSourceDir;
	    
	    public CopyAndUninstallApp(Context context, String packageName, String sourceDir) {
	        mContext = context;
	        mPackageName = packageName;
	        mSourceDir = sourceDir;
	    }
	    
	    @Override
	    public void run() {
	        String tmp = mSourceDir.substring(mSourceDir.lastIndexOf("/")+1);
	        String dest = LocalSetting.getInstance().getLocalFileReceivePath(FileType.FileType_App) + "/" + tmp;
	        File fileSrc = new File(mSourceDir);
	        File fileDest = new File(dest);
	        if (!fileDest.exists()) {
    	        try {
    	            forJavaCp(fileSrc, fileDest);
    	        } catch (Exception e) {
                    Log.e(TAG, "When copy file occour a Error,  " + e.getMessage() + ".  source file = " + mSourceDir);
                }
	        }

	        unInstallApp(mContext, mPackageName);
	    }
	    
	    public static void forJavaCp(File f1,File f2) throws Exception {
	        int length=2097152;    // 2M
	        FileInputStream in=new FileInputStream(f1);
	        FileOutputStream out=new FileOutputStream(f2);
	        byte[] buffer=new byte[length];
	        while(true) {
	            int ins = in.read(buffer);
	            if(ins ==-1) {
	                in.close();
	                out.flush();
	                out.close();
	                return;
	            } else
	                out.write(buffer,0,ins);
	        }
	    }
	}
}
