(function() {
    var app = angular.module("compression");

    app.controller("archiveController", function($scope, ajax, $interval) {
        $scope.archiveItems = [];
		var intervalPromise;

        $scope.reloadItems = function() {
            ajax("GET", "archive", "jobs", {}, function(data) {
                $scope.archiveItems = data.items;
            });
        }
		
		$scope.init = function() {
			$scope.reloadItems();
			intervalPromise = $interval($scope.reloadItems, 5000);
			$scope.$on('$destroy',function(){
				if(intervalPromise)
					$interval.cancel(intervalPromise);   
			});
		}
        
        $scope.init();
    });
})();
