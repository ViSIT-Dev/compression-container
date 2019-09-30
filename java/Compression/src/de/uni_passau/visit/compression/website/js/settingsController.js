(function() {
    var app = angular.module("compression");

    app.controller("settingsController", function($scope, ajax, $location) {
        $scope.apiPort = 0;
        
        $scope.apiAccessWhitelist = [];
        $scope.newWhitelistEntry = "";
        
        $scope.autostart = false;
        $scope.queueMaxLength = 0;
        $scope.defaultLevels = [];
        $scope.newLevelSize = 1000;
        
        $scope.imageCompressionLevels = [];
        $scope.newImageCompressionLevelTitle = "";
        $scope.newImageCompressionLevelMaxWidth = 1920;
        $scope.newImageCompressionLevelMaxHeight = 1080;
        
        $scope.textureLevelLimits = [];
        $scope.textureLevelSizes = [];
        $scope.greatestTextureSize = 0;
        $scope.textureLevelIndices = [];
        
        $scope.textureSizeOptions = [128, 256, 512, 1024, 2048, 4096, 8192, 16384];
        
        $scope.reload = function() {
            ajax("GET", "settings", "config", {}, function(data) {
                $scope.apiPort = data.config.apiPort;
                $scope.apiAccessWhitelist = data.config.apiAccessWhitelist;
                $scope.autostart = data.config.autostart;
                $scope.queueMaxLength = data.config.queueMaxLength;
                $scope.defaultLevels = data.config.defaultLevels;
                $scope.imageCompressionLevels = data.config.imageCompressionLevels;
                $scope.greatestTextureSize = data.config.textureLevelSizes.pop();
                $scope.textureLevelSizes = data.config.textureLevelSizes;
                $scope.textureLevelLimits = data.config.textureLevelLimits;
                $scope.textureLevelIndices = [];
                
                for(var i = 0; i < $scope.textureLevelLimits.length; ++i) {
                    $scope.textureLevelIndices.push(i);
                }
            });
        }

        $scope.submit = function() {
            if($scope.isFormValid()) {
                var newTextureLevelSizes = $scope.textureLevelSizes;
                newTextureLevelSizes.push($scope.greatestTextureSize);
                
                var data = {
                    apiPort: parseInt($scope.apiPort),
                    apiAccessWhitelist: $scope.apiAccessWhitelist,
                    autostart: $scope.autostart,
                    queueMaxLength: parseInt($scope.queueMaxLength),
                    defaultLevels: $scope.defaultLevels,
                    imageCompressionLevels: $scope.imageCompressionLevels,
                    textureLevelLimits: $scope.textureLevelLimits,
                    textureLevelSizes: newTextureLevelSizes
                };
            
                ajax("PUT", "settings", "config", data, function(data) {
                    $scope.reload();
                });
            } 
        }
        
        $scope.isFormValid = function() {
            return $scope.configForm.$valid && $scope.defaultLevels.length > 0 && $scope.apiAccessWhitelist.length > 0;
        }
        
        $scope.deleteLevel = function(level) {
            var index = $scope.defaultLevels.indexOf(level);
            if(index > -1) {
                $scope.defaultLevels.splice(index, 1);
            }
        }
        
        $scope.addLevel = function() {
            if($scope.isAddLevelValid()) {
                $scope.defaultLevels.push(parseInt($scope.newLevelSize) + "");
                $scope.newLevelSize = "";
            }
        }
        
        $scope.isAddLevelValid = function() {
            if(parseInt($scope.newLevelSize) !== NaN) {
            	return !$scope.defaultLevels.includes(parseInt($scope.newLevelSize) + "");
            } 
            
            return false;
        }
        
        $scope.addTextureLimit = function() {
            $scope.textureLevelLimits.push($scope.textureLevelLimits[$scope.textureLevelLimits.length - 1] + 1);
            $scope.textureLevelSizes.push($scope.textureLevelSizes[$scope.textureLevelSizes.length - 1]);
            $scope.textureLevelIndices.push($scope.textureLevelIndices.length);
        }
        
        $scope.removeTextureLimit = function() {
            $scope.textureLevelSizes.pop();
            $scope.textureLevelLimits.pop();
            $scope.textureLevelIndices.pop();
        }
        
        $scope.deleteWhitelistEntry = function(entry) {
            var index = $scope.apiAccessWhitelist.indexOf(entry);
            if(index > -1) {
                $scope.apiAccessWhitelist.splice(index, 1);
            }
        }
        
        $scope.addWhitelistEntry = function(entry) {
            if($scope.isAddWhitelistEntryValid(entry)) {
                $scope.apiAccessWhitelist.push(entry.trim());    
            }
                
            $scope.newWhitelistEntry = "";
        }
        
        $scope.isAddWhitelistEntryValid = function(entry) {
            return entry.trim() != "" && !$scope.apiAccessWhitelist.includes(entry.trim());
        }
        
        $scope.deleteImageCompressionLevel = function(entry) {
            var index = $scope.imageCompressionLevels.indexOf(entry);
            if(index > -1) {
                $scope.imageCompressionLevels.splice(index, 1);
            }
        }
        
        $scope.addImageCompressionLevel = function() {
            if($scope.isAddImageCompressionLevelValid()) {
                $scope.imageCompressionLevels.push({title: $scope.newImageCompressionLevelTitle.trim(), maxWidth: $scope.newImageCompressionLevelMaxWidth, maxHeight: $scope.newImageCompressionLevelMaxHeight});
                $scope.newImageCompressionLevelTitle = "";
                $scope.newImageCompressionLevelMaxWidth = "";
                $scope.newImageCompressionLevelMaxHeight = "";
            }
        }
        
        $scope.isAddImageCompressionLevelValid = function() {
            if($scope.newImageCompressionLevelTitle != "" && $scope.configForm.newImageCompressionLevelTitle.$valid && $scope.isPositiveInteger($scope.newImageCompressionLevelMaxWidth) && $scope.isPositiveInteger($scope.newImageCompressionLevelMaxHeight)) {
                for(var i = 0; i < $scope.imageCompressionLevels.length; ++i) {
                    if($scope.imageCompressionLevels[i].title == $scope.newImageCompressionLevelTitle) {
                        return false;
                    }
                }
                
                return true;
            } else {
                return false;
            }
        }
        
        $scope.isPositiveInteger = function(val) {
            return Number.isInteger(parseFloat(val)) && parseInt(val) >= 1;
        }
        
        $scope.checkLevelLimit = function(index) {
            if(index < $scope.textureLevelIndices.length - 1 && $scope.textureLevelLimits[index] > $scope.textureLevelLimits[index + 1]) {
                $scope.textureLevelLimits[index] = $scope.textureLevelLimits[index + 1];
            }
            
            if(index > 0 && $scope.textureLevelLimits[index] < $scope.textureLevelLimits[index - 1]) {
                $scope.textureLevelLimits[index] = $scope.textureLevelLimits[index - 1];
            }
        }
        
        $scope.init = function() {
            $scope.reload();
        }
        
        $scope.init();
    });
})();
