(function() {
    var app = angular.module("compression");

    app.controller("overviewJobsController", function($scope, ajax, $interval) {
        $scope.queueItems = [];
		var intervalPromise;

        $scope.reloadJobs = function() {
            ajax("GET", "jobs", "queue", {}, function(data) {
                $scope.queueItems = data.items;
            });
        }
		
		$scope.cancelJob = function(jobId, jobTitle) {
			if(confirm("Möchten Sie den gewählten Auftrag '"+jobTitle+"' wirklich abbrechen?")) {
				ajax("DELETE", "jobs", "cancel/"+jobId, null, $scope.reloadJobs);
			}
		}
		
		$scope.init = function() {
			$scope.reloadJobs();
			intervalPromise = $interval($scope.reloadJobs, 5000);
			$scope.$on('$destroy',function(){
				if(intervalPromise)
					$interval.cancel(intervalPromise);   
			});
		}
        
        $scope.init();
    });
})();
