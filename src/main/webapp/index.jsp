
<!DOCTYPE html>
<html lang="en" ng-app="orator">
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
	    <meta name="description" content="">
		
		<title>Orator</title>
		
		<!-- thirdparty css -->
		<link href="./thirdparty/css/bootstrap.min.css" rel="stylesheet">
		<link href="./thirdparty/css/xeditable.css" rel="stylesheet">
		
		<!-- application css -->
		<link href="./css/orator.css" rel="stylesheet">
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
				<!-- Collect the nav links, forms, and other content for toggling -->
			    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
			      <ul class="nav navbar-nav">
			        <li><a href="#">Read!</a></li>
			        <li><a href="#">Books</a></li>
			      </ul>
			      <ul class="nav navbar-nav navbar-right">
			        <li><a href="./logout">logout</a></li>
			      </ul>
			    </div><!-- /.navbar-collapse -->
				<!--/.nav-collapse -->
			</div>
		</div>
	
		<!-- login form body container -->
		<div class="container">
			<div class="row">
				<!-- body content -->
				<div ui-view class="col-sm-12 main">
	
				</div>
				<!-- /end body content -->
			</div>
	
		</div>
		<!-- /.container -->
	
		<!-- required third party libraries -->
		<script src="./thirdparty/js/jquery-2.1.0.min.js"></script>
		<script src="./thirdparty/js/bootstrap.min.js"></script>
				
		<!-- angular -->
		<script src="./thirdparty/js/angular-file-upload-html5-shim.min.js"></script>
		<script src="./thirdparty/js/angular-1.3.0-b5.min.js"></script>
		<script src="./thirdparty/js/angular-1.3.0-b5-route.min.js"></script>
		<script src="./thirdparty/js/angular-1.3.0-b5-resource.min.js"></script>
		<script src="./thirdparty/js/angular-xeditable.min.js"></script>
		<script src="./thirdparty/js/angular-file-upload.min.js"></script>
		
		<!-- angular ui components -->
		<script src="./thirdparty/js/angular-ui-router.min.js"></script>
				
		<!-- orator shared logic -->
		<script src="./app/js/orator.js"></script>
		<script src="./app/js/services/books.service.js"></script>
		<script src="./app/js/services/sessions.service.js"></script>
		
		<!-- orator list logic -->
		<script src="./app/js/controllers/books.js"></script>
		<script src="./app/js/controllers/sessions.js"></script>
		
		<!-- orator individual edit/set logic -->
		<script src="./app/js/controllers/book.js"></script>
		<script src="./app/js/controllers/session.js"></script>

	</body>
</html>
