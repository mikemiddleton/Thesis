<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!--
<!DOCTYPE html>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Demo with JSP</title>
	</head>
	 <body>
        <form method="post" action="validate.jsp">
            <center>
            <table border="1" cellpadding="5" cellspacing="2">
                <thead>
                    <tr>
                        <th colspan="2">Login Here</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Username</td>
                        <td><input type="text" name="username" required/></td>
                    </tr>
                    <tr>
                        <td>Password</td>
                        <td><input type="password" name="password" required/></td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input type="submit" value="Login" />
                            &nbsp;&nbsp;
                            <input type="reset" value="Reset" />
                        </td>                        
                    </tr>                    
                </tbody>
            </table>
            </center>
        </form>
    </body>
</html>
-->



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
	    <title>Login Page</title>
        
		 
        <link type="text/css" rel="stylesheet" href="style/cas.css" />       
        <link href="style/sega.css" rel="stylesheet" type="text/css" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	    <link rel="icon" href="style/favicon.ico" type="image/x-icon" />
	</head>
	<!--<body id="cas" class="fl-theme-iphone">-->
    <body>
<div class="form_content_wrapper">
  <div class="form_content">
    <div class="body_header">SEGA Central Authentication Service </div>
    <div class="post_container">





      <div class="post_header"> Enter Your Username and Password </div>
      <!--postHeader-->
      <div class="post_content">
     	<form id="fm1" class="fm-v clearfix" action="validate.jsp" method="post">
                  
                <!-- Congratulations on bringing CAS online!  The default authentication handler authenticates where usernames equal passwords: go ahead, try it out. -->
                    <h2>Enter your Username and Password</h2>
                    <div class="row fl-controls-left">
                        <label for="username" class="fl-label"><span class="accesskey">U</span>sername:</label>
						

						
						
						<input id="username" name="username" class="required" tabindex="1" accesskey="u" type="text" value="" size="25" autocomplete="false"/>
						
                    </div>
                    <div class="row fl-controls-left">
                        <label for="password" class="fl-label"><span class="accesskey">P</span>assword:</label>
						
						
						<input id="password" name="password" class="required" tabindex="2" accesskey="p" type="password" value="" size="25" autocomplete="off"/>
                    </div>
                    <!--<div class="row check">
                        <input id="warn" name="warn" value="true" tabindex="3" accesskey="w" type="checkbox" />
                        <label for="warn"><span class="accesskey">W</span>arn me before logging me into other sites.</label>
                        
                    </div>
                    -->
                    <div class="row btn-row">
						<input type="hidden" name="lt" value="LT-4-K7RTnelfJMSQk9wowJ3nt5xerIyFaj" />
						<input type="hidden" name="execution" value="e2s1" />
						<input type="hidden" name="_eventId" value="submit" />

                        <input class="btn-submit" name="submit" accesskey="l" value="LOGIN" tabindex="4" type="submit" />
                        <input class="btn-reset" name="reset" accesskey="c" value="CLEAR" tabindex="5" type="reset" />
                    </div>
            </form>
        
        
        <h3> You have reached this page because you have tried to access an area that requires authentication.</h3>
        <p> Please log in using the credentials associated with your <a class="simpleItalic" target="_blank" href="http://www.sega.nau.edu/">SEGA</a> account. If you do not have a <a class="simpleItalic" target="_blank" href="http://www.sega.nau.edu/">SEGA</a> account, please contact a system administrator that can be found using the 'Contact' link at the bottom on the <a class="simpleItalic" target="_blank" href="http://wisard-serv1.egr.nau.edu:8080">SEGA Web Portal</a> . </p>

        <p class="fl-panel fl-note fl-bevel-white fl-font-size-80">For security reasons, please Log Out and Exit your web browser when you are done accessing services that require authentication!</p>
      </div>




	</div>
    </div>
    </div>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
        <!--<script type="text/javascript" src="/cas/js/cas.js"></script>-->
    </body>
</html>



