(function() {
    var app = angular.module("compression");

    app.controller("overviewStateController", function($scope, ajax, $interval) {
        $scope.currentStateStr = "Wird geladen...";
        $scope.currentState = "UNDEFINED";

        $scope.reloadState = function() {
            ajax("GET", "control", "state", {}, function(data) {
                $scope.currentState = data.state;
                $scope.currentStateStr = $scope.translateState(data.state);
            });
        }
        
        $scope.translateState = function(state) {
            switch(state) {
                case "STARTUP": return "Wird/wurde gestartet";
                case "RUNNING": return "Verarbeitung läuft";
                case "PAUSED": return "Verarbeitung pausiert";
                case "SHUTTINGDOWN": return "Wird heruntergefahren";
                case "SHUTDOWN": return "Wurde heruntergefahren";
                default: return "Unbekannt";
            }
        }
		
		$scope.updateState = function(state) {
            ajax("PUT", "control", "state", {state: state}, function(data) {
                $scope.reloadState();
            });
		}
		
		$scope.init = function() {
			$scope.reloadState();
		}
        
        $scope.init();
    });
})();
