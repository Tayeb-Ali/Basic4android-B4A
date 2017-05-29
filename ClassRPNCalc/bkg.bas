Type=StaticCode
Version=6.5
ModulesStructureVersion=1
B4A=true
@EndOfDesignText@
'Code module
'Subs in this code module will be accessible from all modules.
Sub Process_Globals
	'These global variables will be declared once when the application starts.
	'These variables can be accessed from all modules.

End Sub

Sub SetBackground(ColEnabled0 As Int, ColEnabled1 As Int, ColPresed0 As Int, ColPresed1 As Int, Radius As Int) As StateListDrawable
	Dim gdw0, gdw1 As GradientDrawable
	Dim sld As StateListDrawable
	' Define two gradient colors for Enabled state
	Dim colsEnabled(2) As Int
	colsEnabled(0) = ColEnabled0
	colsEnabled(1) = ColEnabled1
	' Define a GradientDrawable for Enabled state
	gdw0.Initialize("TOP_BOTTOM",colsEnabled)
	gdw0.CornerRadius = Radius

	' Define two gradient colors for Pressed state
	Dim colsPressed(2) As Int
	colsPressed(0) = ColPresed0
	colsPressed(1) = ColPresed1
	' Define a GradientDrawable for Pressed state
	gdw1.Initialize("BOTTOM_TOP",colsPressed)
	gdw1.CornerRadius = Radius
	' Define a StateListDrawable
	sld.Initialize
	Dim states(2) As Int
	states(0) = sld.state_enabled
	states(1) = -sld.state_pressed
	sld.addState2(states, gdw0)
	Dim states(1) As Int
	states(0) = sld.state_pressed
	sld.addState2(states, gdw1)
	
	Return sld
End Sub

