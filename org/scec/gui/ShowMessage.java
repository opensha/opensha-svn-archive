<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html lang="en">

<head>

<title>Palm OS Programmer's FAQ</title>

<link rel="Stylesheet" type="text/css" href="./faq.css">

</head>

<body bgcolor="#ffffee" text="#000000" link="#491e00" vlink="#7d2e01" alink="#da7417">

<script>
<!--
	var bName = navigator.appName;
	var bVer = parseInt(navigator.appVersion);
	var ver;
	if (bName == "Netscape" && bVer >= 4) {
		ver = 4;
	} else if (bName == "Microsoft Internet Explorer" && bVer >= 4) {
		ver = 4;
	} else {
		ver = 0;
	}
	if (ver >= 4) {
		document.write('<img src="/wusage_screen_properties.gif?' +
			'width=' + screen.width	+ '&' +
			'height=' + screen.height + '&' +
			'depth=' + screen.colorDepth + '">'); 	
		document.writeln();
	}
// -->
</script>


<!--  ---- Header Bar ----  -->

<table border="0" width="95%" bgcolor="#60B070" cellpadding="5" cellspacing="3" align="center">
	<tr>
		<td align="center" bgcolor="#409040">
				<img src="./bitmaps/stop.gif" alt="" width=20
						height=20>
		</td>


		<td align="center">
			<font face=Verdana,Arial,Helvetica color="#303030">
				<p align=center class=bigger3><b>
				Palm OS Programmer's FAQ<br>
				</b></p>
			</font>	
		</td>

		<td align="center" bgcolor="#409040">
				<a href="intro.html">
						<img src="./bitmaps/rtarrow.gif" 
						alt="" width=20 height=20 border=0></a>
		</td>
	</tr>
</table>


<!--  ---- Body Table ----  -->

<table width="95%" border="0" cellpadding="10">
	<tr valign="top">
		<td>

<!-- FreeFind No Index Page -->

<p class=lmargin>This is the Palm OS Programmer's <a href="http://info.astrian.net/jargon/terms/f/FAQ.html">FAQ</a>. See
the <a href="intro.html">Introduction</a> section for history, a list
of mirrors, and information about the maintainer.</p>

<p class=lmargin>This FAQ answers the most commonly-asked questions
on the <a href="news://news.falch.net/pilot.programmer">Palm
programming newsgroups</a>. The FAQ also contains a growing repository
of Palm OS programming information and links useful for all levels of
programmers. Please <a href="mailto:tangent@cyberport.com">email me</a>
if you have any corrections or additions for the list.</p>

<p class=lmargin>If you would like to view this FAQ off-line, you can
download a <a href="palmfaq.zip">ZIPped version</a> of these pages. (230
KB, last packaged 2002.01.14)</p>


<h3>What's New?</h3>

<p><b>2002.01.14</b><br><p class=inset> Ben Combee contributed updated versions of
some of the functions in the <a href="articles/float.html">floating-point
article</a>. The new versions are cleaner and they compile with current
versions of CodeWarrior and GCC.

<p><b>2001.12.12</b><br><p class=inset> Clarified the <a
href="articles/debug-gdb.html">PRC-Tools debugging article</a>.</p>

<p><b><a href="history.html">Previous "What's New"
Entries...</a></b></p>


<p><a href="search.html"><img src="bitmaps/searchzoom.gif" border=0
alt="Search" width=54 height=22 hspace=0></a>
<img src="bitmaps/dot-clear.gif" width=6 height=30 alt="" valign=center>
<font size=+1>the FAQ</font></p>
<br clear=all>


<h3>Contents</h3>



<p><b>Section 0 - <a href="intro