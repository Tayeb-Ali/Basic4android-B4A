Type=Class
Version=6.5
ModulesStructureVersion=1
B4A=true
@EndOfDesignText@
#Event: Close As Double
'RPNCalc Class module
Sub Class_Globals
	Private CallBack As Object
	Private EventName As String
	Private Parent As Activity
	Private ClassVersion As String	: ClassVersion = "3.0"
	
	Private RegX As Double				: RegX = 1.25
	Private RegY As Double				: RegY = 0
	Private RegZ As Double				: RegZ = 0
	Private RegT As Double				: RegT = 0
	Private Reg As Double					: Reg = 0
	
	Private sRegX As String				: sRegX = "1.25"
	Private sReg As String				: sReg = "0"
		
	Private NRound As Int					: NRound = 3
	
	Private flgEnter As Int				: flgEnter = 0		'flag for Enter mode 0 = standard  1 =   2 = 
	Private flgDot As Int					: flgDot = 0			'flag if dor already entered
	Private flgE As Int						: flgE = 0				'flag if E already entered
	Private flgDeg As Int					: flgDeg = 1			'0 = deg  1 = rad
	Private flgSTO As Int					: flgSTO = 0			'flag if STO key pressed
	Private flgRCL As Int					: flgRCL = 0			'flag if RCL key pressed
	Private flgRound As Int				: flgRound = 0		'flag if Round is active
	Private flgSCI As Int					: flgSCI = 0			'flag if scientific notation is active
	Private flgClX As Int					: flgClX = 0			'flag if ClX pressed
	
	Private Im As Int							: Im = 1
	Private Mem(10) As Int
	Private sVal As String				: sVal = "0"
	Private Val As Double					: Val = 0
	Private ValM As Double				: ValM = 0
	Private ValL As Double				: ValL = 0
	Private Orientations As Int	

	Private lblRegX, lblRegY, lblRegZ, lblRegT As Label
	Private pnlMain, pnlCalculator, pnlFunctions As Panel
	Private btnFunc, btnSTO, btnRCL, btnDEG, btnRAD, btnRound, btnSci As Button
	Private btnx2, btnyx, btnExp, btnExp10 As Button
	Private sdwFunc(2), sdwSTO(2), sdwRCL(2), sdwDEG(2), sdwRAD(2), sdwRound(2), sdwSci(2) As StateListDrawable
	Private lblMemory As Label
	Private ReturnView As Label
End Sub

'Initializes the object. You can add parameters to this method if needed.
Public Sub Initialize(vCallback As Object, vEventName As String)
	CallBack = vCallback
	EventName = vEventName
End Sub

Public Sub DesignerCreateView(base As Panel, lbl As Label, props As Map)
	pnlMain = base
	InitCalc
End Sub

'Adds a RPNCalc customview by code
'cParent is the parant activity
Public Sub AddRPNCalc(cParent As Activity)
	Parent = cParent
	pnlMain.Initialize("pnlMain")
	Parent.AddView(pnlMain, 0, 0, 100%x, 100%y)
	InitCalc
End Sub

'Shows the RPNCalc customview
'XValue is the value to set in register X
'ReturnView is an EditText where the value of the X register will be returned
'If ReturnView = Null then the Close event will be raised
'If ReturnView = an EditText then the Close event will NOT be raised
Public Sub Show(XValue As Double, cReturnView As Object)
	RegX = XValue
	sRegX = XValue
	pnlMain.Visible = True
	Display(1, RegX)
	ReturnView = cReturnView
End Sub

Private Sub InitCalc
	pnlMain.Visible = True

	pnlMain.LoadLayout("rpncalcmain")
	pnlCalculator.LoadLayout("calculator")
	pnlFunctions.LoadLayout("functions")
	
	pnlFunctions.BringToFront
	pnlFunctions.Visible = False
	
	lblMemory.BringToFront

	Scale.SetRate(0.4)
	
	Dim ph As Phone
		
	'check if pnlCalculator.Height with portrait scaling is higher than the screen height in landscape
	If Scale.GetDevicePhysicalSize < 6 Or 2 * pnlCalculator.Width * Scale.GetScaleX_P > Max(100%y, 100%x) Then
		' if yes set only portrait orientation
		Orientations = 1
	Else
		' if no authorize both orientations
		Orientations = -1
	End If
		ph.SetScreenOrientation(Orientations)
	
	Scale.ScaleAll(Parent, True)
	' check if the pnlCalculator.Width + pnlFunctions.Width smaller than the screen width
	If 2 * pnlCalculator.Width < 100%x Then
		' if yes, put pnlCalculator and pnlFunctions side by side and hide btnFunc
		pnlCalculator.Left = 50%x - pnlCalculator.Width
		pnlFunctions.Left = 50%x
		pnlFunctions.Visible = True
'		btnFunc.Visible = False
		btnFunc.Text = "Exit"
	Else
		' if no, show only pnlCalculator hide pnlFunctions and leave btnFunc visible 
		pnlCalculator.Left = (100%x - pnlCalculator.Width) / 2
		pnlCalculator.Top = (100%y - pnlCalculator.Height) / 2
		pnlFunctions.Left = pnlCalculator.Left
		pnlFunctions.Top = pnlCalculator.Top + pnlCalculator.Height - pnlFunctions.Height
		pnlFunctions.Visible = False
		btnFunc.Text = "Fn/Ex"
	End If
	
	InitBackgrounds
	btnFunc.Background = sdwFunc(0)
	
	Dim rch As RichString
	rch.Initialize("10x") 
	rch.Superscript(2, 3)
	btnExp10.Text = rch
	
	Dim rch As RichString
	rch.Initialize("ex") 
	rch.Superscript(1, 2)
	btnExp.Text = rch

	Dim rch As RichString
	rch.Initialize("yx") 
	rch.Superscript(1, 2)
	btnyx.Text = rch

	Dim rch As RichString
	rch.Initialize("x2") 
	rch.Superscript(1, 2)
	btnx2.Text = rch
	
	SetDEG_RAD_Colors
End Sub

Private Sub pnlMain_Click
	' consumes the event
End Sub

Private Sub btnOperation_Click
	Dim btn As Button
	GetRegX

	btn = Sender
	Select btn.Tag
	Case "-"
		RegX = RegY-RegX
	Case "+"
		RegX = RegX+RegY
	Case "*"
		RegX = RegX*RegY
	Case "/"
		If RegX = 0 Then
			lblRegX.Text = "Error Div 0"
			Return
		Else
			RegX = RegY / RegX
		End If
	Case "yx"
		RegX = Power(RegY, RegX)
	Case "%"
		RegX = RegY / 100 * RegX
	End Select

	ScrollDown
End Sub

Private Sub btnNumber_Click
	Dim btn As Button
	Dim n As Double
	
	btn = Sender
	
	n = btn.Tag
	Reg = n
	sReg = n
	Number
End Sub

Private Sub btnE_Click
	If flgE = 0 Then
		If flgEnter = 0 OR flgEnter = 2 Then
			ScrollUp
			sRegX = "1E"
			flgEnter = 1
		Else
			sRegX = sRegX & "E"
		End If
		lblRegX.Text = sRegX
		flgE = 1
		flgDot = 1
	End If
End Sub

Private Sub btnDot_Click
	If flgDot = 0 Then
		If flgEnter = 0 OR flgEnter = 2 Then
			ScrollUp
			sRegX = "0."
			flgEnter = 1
		Else
			sRegX = sRegX & "."
		End If
		lblRegX.Text = sRegX
		flgDot = 1
	End If
End Sub

Private Sub btnScroll_Click
	RegX = RegY
	sRegX = RegX
	RegY = RegZ
	RegZ = RegT
	Display(1, RegX)
	Display(2, RegY)
	Display(3, RegZ)
	flgDot = 0
	flgEnter = 0
	flgE = 0
End Sub

Private Sub btnChSign_Click
	Dim i As Int
	
	If flgE = 0 Then
		RegX = -RegX
		
		If flgEnter = 1 Then
			If sRegX.CharAt(0) = "-" Then
				sRegX = sRegX.Replace("-", " ")
			Else
				sRegX = sRegX.Replace(" ", "-")
			End If
			lblRegX.Text = sRegX
		Else
			sRegX = RegX
			Display(1, RegX)
		End If
	Else
		Log(sRegX.Length)
		Log("*"&sRegX&"*")
		For i = sRegX.Length - 2 To 0 Step -1
			If sRegX.CharAt(i) = "E" Then Exit
		Next
		
		If sRegX.CharAt(i+1) = "-" Then
		  sRegX = sRegX.Replace("-", "")
		Else
		  sRegX = sRegX.Replace("E", "E-")
		End If
		lblRegX.Text = sRegX
	End If
End Sub

Private Sub btnFunction_Click
	Dim btn As Button
	
	btn = Sender
	GetRegX

	Select btn.Tag
	Case "Pi"
		ScrollUp
		RegX = cPI
	Case "Log"
		RegX = Logarithm(RegX, 10)
	Case "Ln"
		RegX = Logarithm(RegX, cE)
	Case "Exp10"
		RegX = Power(10, RegX)
	Case "Exp"
		RegX = Power(cE, RegX)
	Case "Sqrt"
		RegX = Sqrt(RegX)
	Case "x2"
		RegX = RegX * RegX
	Case "R>D"
		RegX = RegX * 180 / cPI
	Case "D>R"
		RegX = RegX * cPI / 180
	Case "Tan"
		If flgDeg = 1 Then
			RegX = TanD(RegX)
		Else
			RegX = Tan(RegX)
		End If	
	Case "ATan"
		If flgDeg = 1 Then
			RegX = ATanD(RegX)
		Else
			RegX = ATan(RegX)
		End If
	Case "Sin"
		If flgDeg = 1 Then
			RegX = SinD(RegX)
		Else
			RegX = Sin(RegX)
		End If	
	Case "ASin"
		If RegX <= 1 Then
			If flgDeg = 1 Then
				RegX = ASinD(RegX)
			Else
				RegX = ASin(RegX)
			End If		
		Else
			lblRegX.Text = "Error X > 1"
			Return
		End If
	Case "Cos"
		If flgDeg = 1 Then
			RegX = CosD(RegX)
		Else
			RegX = Cos(RegX)
		End If	
	Case "ACos"
		If RegX <= 1 Then
			If flgDeg = 1 Then
				RegX = ACosD(RegX)
			Else
				RegX = ACos(RegX)
			End If		
		Else
			lblRegX.Text = "Error X > 1"	
			Return
		End If
	Case "x y"
		Reg = RegY
		RegY = RegX
		RegX = Reg
		Display(2, RegY)
	Case "1/x"
		If RegX	<>	0 Then
			RegX	=	1/RegX
		Else
			lblRegX.Text	=	"Error Div 0"
			Return
		End If
	End Select
	EndFunction
End Sub

Private Sub btnEnter_Click
	GetRegX

	RegT = RegZ
	RegZ = RegY
	RegY = RegX
		
	Display(1, RegX)
	Display(2, RegY)
	Display(3, RegZ)
	Display(4, RegT)

	flgEnter = 2
	flgDot = 0
	flgE = 0
End Sub

Private Sub Display(IR As Int, Val0 As Double)
	If flgSCI = 1 Then
		If NRound = 0 Then
			lblRegX.TextSize = 16 * Scale.GetScaleY
			lblRegY.TextSize = lblRegX.TextSize
			lblRegZ.TextSize = lblRegX.TextSize
			lblRegT.TextSize = lblRegX.TextSize
		Else If NRound < 7 Then
			lblRegX.TextSize = 24 * Scale.GetScaleY
			lblRegY.TextSize = lblRegX.TextSize
			lblRegZ.TextSize = lblRegX.TextSize
			lblRegT.TextSize = lblRegX.TextSize
		Else 
			lblRegX.TextSize = 22 * Scale.GetScaleY
			lblRegY.TextSize = lblRegX.TextSize
			lblRegZ.TextSize = lblRegX.TextSize
			lblRegT.TextSize = lblRegX.TextSize
		End If
	Else
		If NRound = 0 Then
			lblRegX.TextSize = 20 * Scale.GetScaleY
			lblRegY.TextSize = lblRegX.TextSize
			lblRegZ.TextSize = lblRegX.TextSize
			lblRegT.TextSize = lblRegX.TextSize
		Else
			lblRegX.TextSize = 24 * Scale.GetScaleY
			lblRegY.TextSize = lblRegX.TextSize
			lblRegZ.TextSize = lblRegX.TextSize
			lblRegT.TextSize = lblRegX.TextSize
		End If
	End If

	If flgSCI = 1 Then
		If Val0 <> 0 AND Val0 <> "0.0" Then
			Val = Logarithm(Abs(Val0), 10)
			ValM = Floor(Val)
			ValL = Val - ValM
			Val = Power(10,ValL)
		Else
			Val = 0
			ValM = 0
		End If
	
		If NRound > 0 Then
			Val = Abs(Round2(Val, NRound))
			sVal = Val
			AddZeros
		Else
			sVal = Abs(Val)
		End If
		
		sVal = sVal & "E"
		If ValM < 0 Then
			sVal = sVal & "-"
		Else
			sVal = sVal & "+"
		End If

		If Val0 < 0 Then
			sVal = "-" & sVal
		Else If flgEnter=0 Then
			sVal = " " & sVal
		End If

		If Abs(ValM) < 100 Then
			sVal = sVal & "0"
		End If
		If Abs(ValM) < 10 Then
			sVal = sVal & "0"
		End If
		sVal = sVal & (Abs(ValM))
	Else
		If NRound > 0 Then
			Val = Abs(Round2(Val0, NRound))
			sVal = Val
			AddZeros
		Else
			Val = Abs(Val0)
			sVal = Val
		End If

		If Val0 < 0 Then
			sVal = "-" & sVal
		Else
			sVal = " " & sVal
		End If
	End If
	
	Select IR
	Case 1
		lblRegX.Text = sVal
	Case 2
		lblRegY.Text = sVal
	Case 3
		lblRegZ.Text = sVal
	Case 4
		lblRegT.Text = sVal
	End Select
End Sub

Private Sub AddZeros
	Dim i, j As Int

	i = sVal.Length - 1
	Do While i > -1
		If sVal.CharAt(i) = "." Then Exit 
		i = i - 1
	Loop

	If i = -1 Then
		i = NRound
		sVal = sVal & "."
	Else
		i = NRound - sVal.Length + i + 1
	End If
	
	If i > 0 Then
		For j = 1 To i
			sVal = sVal & "0"
		Next
	End If
End Sub

Private Sub ScrollDown
	sRegX = RegX
	Display(1, RegX)

	RegY = RegZ
	RegZ = RegT
	Display(3, RegZ)
	Display(2, RegY)
	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub ScrollUp
	RegT = RegZ
	RegZ = RegY
	RegY = RegX
		
	Display(4, RegT)
	Display(3, RegZ)
	Display(2, RegY)
End Sub

Private Sub EndFunction
	Display(1, RegX)
	sRegX = RegX
	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub GetRegX
	If sRegX.CharAt(sRegX.Length - 1) = "E" OR sRegX = "" OR sRegX = " " OR sRegX = "-" Then
		sRegX = sRegX & "0"
	End If
	RegX = sRegX
	Mem(0) = RegX
End Sub

Private Sub Number
	If flgSTO = 1  Then
		If  Reg> = 1 Then
			Im = Reg
			Memory
		End If
	Else If flgRCL = 1 Then
		Im = Reg
		Memory
	Else If flgRound = 1 Then
		NRound = Reg
		SRound
		flgEnter = 0
		flgDot = 0
		flgRound = 0
		flgE = 0
		btnRound.Background = sdwRound(flgRound)
		btnFunc.Background = sdwFunc(0)
	Else
		If flgEnter = 0 Then
			If flgClX = 0 Then
				ScrollUp
			End If
			sRegX = sReg
			flgEnter = 1
		Else If flgEnter = 2 Then
			sRegX = sReg
			flgEnter = 1
		Else
			sRegX = sRegX & sReg
		End If
		
		If sRegX.CharAt(0) <> "-" AND sRegX.CharAt(0) <> " " Then
			sRegX = " " & sRegX
		End If
		lblRegX.Text = sRegX
	End If
	flgClX = 0
End Sub

Private Sub Memory
	If flgSTO = 1 Then
		GetRegX
		EndFunction
		Mem(Im) = RegX
	Else
		If flgEnter = 0 Then
			ScrollUp
		End If
		RegX = Mem(Im)
		sRegX = RegX
		Display(1, RegX)
	End If	
	flgSTO = 0
	btnSTO.Background = sdwSTO(flgSTO)
	flgRCL = 0
	btnRCL.Background = sdwRCL(flgRCL)
	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub SRound
	Display(1, RegX)
	Display(2, RegY)
	Display(3, RegZ)
	Display(4, RegT)
End Sub

Private Sub btnClX_Click
	If lblRegX.Text.SubString2(0,5) <> "Error" Then
		sRegX = "0"
		RegX = 0
		flgEnter = 0
'		flgEnter = 1
		flgDot = 0
		flgE = 0
	End If
	flgClX = 1
	Display(1, RegX)
End Sub

Private Sub btnClAll_Click
	sRegX = "0"
	RegX = 0
	RegY = 0
	RegZ = 0
	RegT = 0
		
	Display(1, RegX)
	Display(2, RegY)
	Display(3, RegZ)
	Display(4, RegT)

	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub btnSTO_Click
	GetRegX
	If flgSTO = 0 Then
		flgSTO = 1
	Else
		flgSTO = 0
	End If
	btnSTO.Background = sdwSTO(flgSTO)
End Sub

Private Sub btnRCL_Click
	If flgRCL = 0 Then
		flgRCL = 1
	Else
		flgRCL = 0
	End If
	btnRCL.Background = sdwRCL(flgRCL)
End Sub

Private Sub btnRectPol_Click
	Reg = sRegX
	RegX = Sqrt(Reg * Reg + RegY * RegY)
	RegY = ATan(RegY / Reg)
	If flgDeg = 1 Then
		RegY = RegY * 180 / cPI
	End If	
	sRegX = RegX
	Display(1, RegX)
	Display(2, RegY)
	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub btnPolRect_Click
	Reg = sRegX
	If flgDeg = 1 Then
		RegY = RegY * cPI / 180
	End If	
	RegX = Reg * Cos(RegY)
	RegY = Reg * Sin(RegY)
	sRegX = RegX
	Display(1, RegX)
	Display(2, RegY)
	flgEnter = 0
	flgDot = 0
	flgE = 0
End Sub

Private Sub btnClM_Click
	Dim i As Int
	
	For i = 1 To 9
		Mem(i) = 0
	Next
End Sub

Private Sub btnBackSpace_Click
	Dim i As Int
	
	If flgEnter = 1 AND sRegX.Length > 1 Then
		If flgE = 1 AND sRegX.CharAt(sRegX.Length - 1) = "E" Then
			flgE = 0
			flgDot = 0
			For i = 0 To sRegX.Length - 1
				If sRegX.CharAt(i) = "." Then
					flgDot = 1
					Return
				End If
			Next
		End If
		If flgDot = 1 AND sRegX.CharAt(sRegX.Length - 1) = "." Then
			flgDot = 0
		End If
		sRegX = sRegX.SubString2(0, sRegX.Length - 1)
		lblRegX.Text = sRegX
	End If
End Sub

Private Sub btnRound_Click
	GetRegX
	If flgRound = 0 Then
		flgRound = 1
	Else
		flgRound = 0
	End If
	btnRound.Background = sdwRound(flgRound)

	If btnFunc.Visible = True AND Orientations = 1 Then
		btnFunc_Click
	End If
End Sub

Private Sub btnSci_Click
	GetRegX
	If flgSCI = 0 Then
		flgSCI = 1
	Else
		flgSCI = 0
	End If
	btnSci.Background = sdwSci(flgSCI)
	
	SRound
	flgEnter = 0
	flgDot = 0
	flgE = 0
	If Orientations = 1 Then
		btnFunc_Click
	End If
End Sub

Private Sub btnDEG_Click
	flgDeg = 1
	SetDEG_RAD_Colors
End Sub

Private Sub btnRAD_Click
	flgDeg = 0
	SetDEG_RAD_Colors
End Sub

Private Sub btnFunc_Click
	If btnFunc.Text = "Exit" Then
		btnFunc_LongClick
	Else
		pnlFunctions.Visible = Not(pnlFunctions.Visible)
		If pnlFunctions.Visible = False Then
			btnFunc.Background = sdwFunc(0)
			btnFunc.TextColor = Colors.White
		Else
			btnFunc.Background = sdwFunc(1)
			btnFunc.TextColor = Colors.Black
		End If
	End If
End Sub

Private Sub btnFunc_LongClick
	pnlMain.Visible = False
	If ReturnView Is Label Then
		ReturnView.Text = lblRegX.Text
	Else
		If SubExists(CallBack, EventName & "_Close") = True Then
			CallSub2(CallBack, EventName & "_Close", RegX)
		End If
	End If
End Sub

Private Sub btnMemory_Click
	Dim txt As String
	
	txt = " Memory" & CRLF
	For i = 0 To 9
		txt = txt & " M" & i & " = " & Mem(i) & CRLF
	Next
	lblMemory.Text = txt
	lblMemory.Visible = Not(lblMemory.Visible)
End Sub

Private Sub lblMemory_Click
	btnMemory_Click
End Sub

Private Sub InitBackgrounds
	sdwFunc(0) = bkg.SetBackground(Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), 5dip)
	sdwFunc(1) = bkg.SetBackground(Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), 5dip)

	sdwDEG(0) = bkg.SetBackground(Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), 5dip)
	sdwDEG(1) = bkg.SetBackground(Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), 5dip)

	sdwRAD(0) = bkg.SetBackground(Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), 5dip)
	sdwRAD(1) = bkg.SetBackground(Colors.RGB(170, 255, 170), Colors.RGB(220, 255, 220), Colors.RGB(200, 255, 200), Colors.RGB(0, 140, 0), 5dip)

	sdwSTO(0) = bkg.SetBackground(Colors.RGB(255, 200, 255), Colors.RGB(200, 0, 200), Colors.RGB(255, 250, 255), Colors.RGB(255, 200, 255), 5dip)
	sdwSTO(1) = bkg.SetBackground(Colors.RGB(255, 250, 255), Colors.RGB(255, 200, 255),Colors.RGB(255, 200, 255), Colors.RGB(200, 0, 200), 5dip)

	sdwRCL(0) = bkg.SetBackground(Colors.RGB(255, 200, 255), Colors.RGB(200, 0, 200), Colors.RGB(255, 250, 255), Colors.RGB(255, 200, 255), 5dip)
	sdwRCL(1) = bkg.SetBackground(Colors.RGB(255, 250, 255), Colors.RGB(255, 200, 255),Colors.RGB(255, 200, 255), Colors.RGB(200, 0, 200), 5dip)

	sdwRound(0) = bkg.SetBackground(Colors.RGB(200, 200, 255), Colors.RGB(0, 0, 200), Colors.RGB(200, 200, 255), Colors.RGB(0, 0, 200), 5dip)
	sdwRound(1) = bkg.SetBackground(Colors.RGB(170, 170, 255), Colors.RGB(220, 220, 255), Colors.RGB(200, 200, 255), Colors.RGB(0, 0, 200), 5dip)

	sdwSci(0) = bkg.SetBackground(Colors.RGB(200, 200, 255), Colors.RGB(0, 0, 200), Colors.RGB(170, 170, 255), Colors.RGB(220, 220, 255), 5dip)
	sdwSci(1) = bkg.SetBackground(Colors.RGB(170, 170, 255), Colors.RGB(220, 220, 255), Colors.RGB(200, 200, 255), Colors.RGB(0, 0, 200), 5dip)
End Sub

Private Sub SetDEG_RAD_Colors
	If flgDeg = 1 Then
		btnDEG.Background = sdwDEG(1)
		btnRAD.Background = sdwRAD(0)
	Else
		btnDEG.Background = sdwDEG(0)
		btnRAD.Background = sdwRAD(1)
	End If
End Sub

'Returns the class version
Sub getVersion As String
	Return ClassVersion
End Sub
