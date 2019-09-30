(function() {
    var app = angular.module("compression");

    app.controller("dispatchController", function($scope, ajax, $location) {
        $scope.basepath = "";
        $scope.objectUid = "";
        $scope.mediaUid = "";
        $scope.filetitle = "";
        $scope.mimetype = "text/plain";
        $scope.notificationemail = "";
        $scope.levels = ["Automatisch"];
        $scope.newLevelSize = 1000;
        $scope.newLevelType = "Fixed";

        $scope.submit = function() {
            if($scope.isFormValid()) {
                var data = {
                    basePath: $scope.basepath,
                    objectUid: $scope.objectUid,
                    mediaUid: $scope.mediaUid,
                    title: $scope.filetitle,
                    mimeType: $scope.mimetype,
                    levels: $scope.levels
                };
                
                ajax("POST", "jobs", "dispatch", data, function(data) {
                    $location.path("/overview");
                });
            }
        }
        
        $scope.isFormValid = function() {
            return $scope.dispatchForm.$valid && $scope.levels.length > 0;
        }
        
        $scope.deleteLevel = function(level) {
            var index = $scope.levels.indexOf(level);
            if(index > -1) {
                $scope.levels.splice(index, 1);
            }
        }
        
        $scope.addLevel = function(levelType, levelSize) {
            if($scope.isAddLevelValid(levelType, levelSize)) {
                if(levelType === "Fixed") {
                    $scope.levels.push(parseInt(levelSize));
                } else {
                    $scope.levels.push(levelType);    
                }
            }
        }
        
        $scope.isAddLevelValid = function(levelType, levelSize) {
            if(levelType === "Automatisch" || (levelType === "Fixed" && parseInt(levelSize) !== NaN)) {
                if(levelType === "Fixed") {
                    return !$scope.levels.includes(parseInt(levelSize));
                } else {
                    return !$scope.levels.includes(levelType);
                }
            } 
            
            return false;
        }
    });
})();
