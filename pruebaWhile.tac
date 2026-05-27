L3:
if x <= y goto L5
goto L2
L5:
if x != 5 goto L4
goto L2
L4:
if a == b goto L6
goto L8
L8:
if c == d goto L6
goto L7
L6:
t1 = x + 1
x = t1
goto L3
L7:
x = 5
goto L3
L2:
if x == 5 goto L9
goto L1
L9:
x = 0
L1: