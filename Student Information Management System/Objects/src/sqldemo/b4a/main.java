package sqldemo.b4a;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "sqldemo.b4a", "sqldemo.b4a.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "sqldemo.b4a", "sqldemo.b4a.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "sqldemo.b4a.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        Object[] o;
        if (permissions.length > 0)
            o = new Object[] {permissions[0], grantResults[0] == 0};
        else
            o = new Object[] {"", false};
        processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.sql.SQL _sql1 = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _cursor1 = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lvdb = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdadd = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmddelete = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdedit = null;
public anywheresoftware.b4a.objects.EditTextWrapper _id = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txt_fname = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txt_lname = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txt_id = null;
public static int _idv = 0;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 29;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 31;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 32;BA.debugLine="If File.Exists(File.DirInternal,\"dbss.sql\") = Fal";
if (anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"dbss.sql")==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 33;BA.debugLine="File.Copy(File.DirAssets,\"dbss.sql\",File.DirInte";
anywheresoftware.b4a.keywords.Common.File.Copy(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"dbss.sql",anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"dbss.sql");
 };
 //BA.debugLineNum = 36;BA.debugLine="If SQL1.IsInitialized = False Then";
if (_sql1.IsInitialized()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 37;BA.debugLine="SQL1.Initialize(File.DirInternal, \"dbss.sql\", Fa";
_sql1.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"dbss.sql",anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 40;BA.debugLine="DBload";
_dbload();
 //BA.debugLineNum = 43;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 49;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 51;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 45;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 47;BA.debugLine="End Sub";
return "";
}
public static String  _cmdadd_click() throws Exception{
int _i = 0;
int _newid = 0;
 //BA.debugLineNum = 66;BA.debugLine="Sub cmdAdd_Click";
 //BA.debugLineNum = 68;BA.debugLine="If txt_Fname.Text = \"\" Or txt_Lname.Text = \"\" Or";
if ((mostCurrent._txt_fname.getText()).equals("") || (mostCurrent._txt_lname.getText()).equals("") || (mostCurrent._txt_id.getText()).equals("")) { 
 //BA.debugLineNum = 69;BA.debugLine="Msgbox(\"You have to enter all fields\",\"Missed da";
anywheresoftware.b4a.keywords.Common.Msgbox("You have to enter all fields","Missed data field",mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 73;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM Student\"";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM Student")));
 //BA.debugLineNum = 74;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 75;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step6 = 1;
final int limit6 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0) ; (step6 > 0 && _i <= limit6) || (step6 < 0 && _i >= limit6); _i = ((int)(0 + _i + step6)) ) {
 //BA.debugLineNum = 76;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 78;BA.debugLine="Dim NewID As Int";
_newid = 0;
 //BA.debugLineNum = 79;BA.debugLine="NewID = cursor1.GetInt(\"Studentid\")";
_newid = _cursor1.GetInt("Studentid");
 }
};
 };
 //BA.debugLineNum = 83;BA.debugLine="NewID = txt_ID.Text ' add 1 to the ID number to m";
_newid = (int)(Double.parseDouble(mostCurrent._txt_id.getText()));
 //BA.debugLineNum = 84;BA.debugLine="SQL1.ExecNonQuery(\"INSERT INTO Student VALUES('\"";
_sql1.ExecNonQuery("INSERT INTO Student VALUES('"+BA.NumberToString(_newid)+"','"+mostCurrent._txt_fname.getText()+"','"+mostCurrent._txt_lname.getText()+"')");
 //BA.debugLineNum = 85;BA.debugLine="DBload";
_dbload();
 //BA.debugLineNum = 86;BA.debugLine="txt_ID.Text = \"\"";
mostCurrent._txt_id.setText((Object)(""));
 //BA.debugLineNum = 87;BA.debugLine="txt_Fname.Text = \"\"";
mostCurrent._txt_fname.setText((Object)(""));
 //BA.debugLineNum = 88;BA.debugLine="txt_Lname.Text = \"\"";
mostCurrent._txt_lname.setText((Object)(""));
 //BA.debugLineNum = 89;BA.debugLine="txt_Fname.RequestFocus";
mostCurrent._txt_fname.RequestFocus();
 };
 //BA.debugLineNum = 93;BA.debugLine="End Sub";
return "";
}
public static String  _cmddelete_click() throws Exception{
 //BA.debugLineNum = 94;BA.debugLine="Sub cmdDelete_Click";
 //BA.debugLineNum = 97;BA.debugLine="SQL1.ExecNonQuery(\"DELETE FROM Student where Stud";
_sql1.ExecNonQuery("DELETE FROM Student where Studentid = '"+mostCurrent._txt_id.getText()+"' ");
 //BA.debugLineNum = 98;BA.debugLine="DBload";
_dbload();
 //BA.debugLineNum = 99;BA.debugLine="txt_ID.Text = \"\"";
mostCurrent._txt_id.setText((Object)(""));
 //BA.debugLineNum = 100;BA.debugLine="txt_Fname.Text = \"\"";
mostCurrent._txt_fname.setText((Object)(""));
 //BA.debugLineNum = 101;BA.debugLine="txt_Lname.Text = \"\"";
mostCurrent._txt_lname.setText((Object)(""));
 //BA.debugLineNum = 103;BA.debugLine="End Sub";
return "";
}
public static String  _cmdedit_click() throws Exception{
 //BA.debugLineNum = 122;BA.debugLine="Sub cmdEdit_Click";
 //BA.debugLineNum = 123;BA.debugLine="If txt_Fname.Text = \"\" Or txt_Lname.Text = \"\" Or";
if ((mostCurrent._txt_fname.getText()).equals("") || (mostCurrent._txt_lname.getText()).equals("") || (mostCurrent._txt_id.getText()).equals("")) { 
 //BA.debugLineNum = 124;BA.debugLine="Msgbox(\"Select item to edit\",\"Missed data item\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Select item to edit","Missed data item",mostCurrent.activityBA);
 }else {
 //BA.debugLineNum = 126;BA.debugLine="SQL1.ExecNonQuery(\"UPDATE  Student set FirstName";
_sql1.ExecNonQuery("UPDATE  Student set FirstName ='"+mostCurrent._txt_fname.getText()+"',LastName ='"+mostCurrent._txt_lname.getText()+"' WHERE Studentid = "+mostCurrent._txt_id.getText());
 //BA.debugLineNum = 127;BA.debugLine="DBload";
_dbload();
 };
 //BA.debugLineNum = 130;BA.debugLine="End Sub";
return "";
}
public static String  _cmdexit_click() throws Exception{
 //BA.debugLineNum = 131;BA.debugLine="Sub cmdExit_Click";
 //BA.debugLineNum = 132;BA.debugLine="Activity.finish";
mostCurrent._activity.Finish();
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _dbload() throws Exception{
int _i = 0;
 //BA.debugLineNum = 53;BA.debugLine="Sub DBload";
 //BA.debugLineNum = 54;BA.debugLine="LVDb.Clear'need to clear the list";
mostCurrent._lvdb.Clear();
 //BA.debugLineNum = 55;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM Student\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM Student")));
 //BA.debugLineNum = 56;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step3 = 1;
final int limit3 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0) ; (step3 > 0 && _i <= limit3) || (step3 < 0 && _i >= limit3); _i = ((int)(0 + _i + step3)) ) {
 //BA.debugLineNum = 57;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 58;BA.debugLine="LVDb.AddSingleLine(cursor1.GetString(\"Studentid\")&";
mostCurrent._lvdb.AddSingleLine(_cursor1.GetString("Studentid")+" | "+_cursor1.GetString("FirstName")+" | "+_cursor1.GetString("LastName"));
 //BA.debugLineNum = 59;BA.debugLine="LVDb.SingleLineLayout.ItemHeight = 80";
mostCurrent._lvdb.getSingleLineLayout().setItemHeight((int) (80));
 //BA.debugLineNum = 60;BA.debugLine="LVDb.SingleLineLayout.Label.TextSize = 15";
mostCurrent._lvdb.getSingleLineLayout().Label.setTextSize((float) (15));
 //BA.debugLineNum = 61;BA.debugLine="LVDb.SingleLineLayout.label.TextColor = Colors.Bla";
mostCurrent._lvdb.getSingleLineLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 62;BA.debugLine="LVDb.SingleLineLayout.label.Color = Colors.White";
mostCurrent._lvdb.getSingleLineLayout().Label.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 }
};
 //BA.debugLineNum = 64;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 17;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 20;BA.debugLine="Dim LVDb As ListView";
mostCurrent._lvdb = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 21;BA.debugLine="Dim cmdAdd As Button";
mostCurrent._cmdadd = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Dim cmdDelete As Button";
mostCurrent._cmddelete = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Dim cmdEdit As Button";
mostCurrent._cmdedit = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 24;BA.debugLine="Dim ID,txt_Fname,txt_Lname,txt_ID As EditText";
mostCurrent._id = new anywheresoftware.b4a.objects.EditTextWrapper();
mostCurrent._txt_fname = new anywheresoftware.b4a.objects.EditTextWrapper();
mostCurrent._txt_lname = new anywheresoftware.b4a.objects.EditTextWrapper();
mostCurrent._txt_id = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Dim idv As Int";
_idv = 0;
 //BA.debugLineNum = 27;BA.debugLine="End Sub";
return "";
}
public static String  _lvdb_itemclick(int _position,Object _value) throws Exception{
String _idvalue = "";
int _countit = 0;
int _i = 0;
 //BA.debugLineNum = 105;BA.debugLine="Sub LVDb_ItemClick (Position As Int, Value As Obje";
 //BA.debugLineNum = 106;BA.debugLine="Dim idvalue As String";
_idvalue = "";
 //BA.debugLineNum = 107;BA.debugLine="Dim countIt As Int";
_countit = 0;
 //BA.debugLineNum = 109;BA.debugLine="idvalue = Value";
_idvalue = BA.ObjectToString(_value);
 //BA.debugLineNum = 110;BA.debugLine="countIt = idvalue.IndexOf(\"|\") 'find location of s";
_countit = _idvalue.indexOf("|");
 //BA.debugLineNum = 111;BA.debugLine="idvalue = idvalue.SubString2(0,countIt) 'find firs";
_idvalue = _idvalue.substring((int) (0),_countit);
 //BA.debugLineNum = 112;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM Student w";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM Student where Studentid = '"+_idvalue+"' ")));
 //BA.debugLineNum = 113;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step7 = 1;
final int limit7 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0) ; (step7 > 0 && _i <= limit7) || (step7 < 0 && _i >= limit7); _i = ((int)(0 + _i + step7)) ) {
 //BA.debugLineNum = 114;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 116;BA.debugLine="txt_ID.Text = cursor1.GetInt(\"Studentid\")";
mostCurrent._txt_id.setText((Object)(_cursor1.GetInt("Studentid")));
 //BA.debugLineNum = 117;BA.debugLine="txt_Fname.Text=cursor1.getString(\"FirstName\")";
mostCurrent._txt_fname.setText((Object)(_cursor1.GetString("FirstName")));
 //BA.debugLineNum = 118;BA.debugLine="txt_Lname.Text=cursor1.getString(\"LastName\")";
mostCurrent._txt_lname.setText((Object)(_cursor1.GetString("LastName")));
 }
};
 //BA.debugLineNum = 120;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 12;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 13;BA.debugLine="Dim SQL1 As SQL";
_sql1 = new anywheresoftware.b4a.sql.SQL();
 //BA.debugLineNum = 14;BA.debugLine="Dim cursor1 As Cursor";
_cursor1 = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 15;BA.debugLine="End Sub";
return "";
}
}
