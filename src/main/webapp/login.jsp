
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=no" />
    <meta name="description" content="">
	
	<title>Orator | Login</title>
	
	<!-- Bootstrap core CSS -->
	<link href="${pageContext.request.contextPath}/thirdparty/css/bootstrap.min.css" rel="stylesheet">
	
	<!-- application css -->
	<link href="${pageContext.request.contextPath}/css/orator.css" rel="stylesheet">
</head>

<body>
	<!-- navbar -->
	<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".navbar-collapse">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">Orator</a>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</div>

	<!-- login form body container -->
	<div class="container">
		<div class="row">


			<!-- body content -->
			<div class="col-sm-offset-3 col-sm-6 main login-well">

				<!-- login form -->
				<form novalidate name="loginForm" role="form" method="POST" action="">

					<!-- form element row -->
					<div class="row">
						<div class="col-sm-12">

							<!-- user name -->
							<div class="form-group has-feedback">
								<label for="loginName" class="control-label">User Name</label> <input
									type="text" class="form-control" name="username" id="loginName" required placeholder="username">
							</div>

							<!-- password -->
							<div class="form-group has-feedback">
								<label for="loginPassword" class="control-label">Password</label>
								<input type="password" class="form-control" name="password" id="loginPassword" required placeholder="password">
							</div>
						</div>
					</div>

					<!-- button row -->
					<div class="row">
						<div class="col-sm-12">
							<div class="form-group">
								<button type="submit" class="btn btn-primary pull-right">login</button>
							</div>
						</div>
					</div>

				</form>
			</div>
			<!-- /end body content -->
		</div>

	</div>
	<!-- /.container -->

	<!-- required third party libraries -->
	<script src="${pageContext.request.contextPath}/thirdparty/js/jquery-2.1.0.min.js"></script>
	<script src="${pageContext.request.contextPath}/thirdparty/js/bootstrap.min.js"></script>
</body>
</html>
