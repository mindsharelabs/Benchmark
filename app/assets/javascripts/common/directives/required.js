/**
 * A benchmark cell directive
 * prints out the value or an error based on the response from the benchmark web server
 * that references them all.
 */
define(['angular','./main'], function(angular) {
    'use strict';

    var mod = angular.module('common.directives');
    mod.directive('required', [function() {
//        var validNodes = ['input', 'select'];
        return {
            restrict: 'C',
            controller: ["$scope", "$element", function ($scope, $element) {
                // the name of the field in the forms hash;
                var formInputName = $element.find("[required]").attr("name");
                console.log(formInputName);

                // if there is a name then attach the validation handlers
                if (formInputName !== undefined) {
                    // it is expected that the scope contains form.baselineForm.  The baseline
                    // controller will set this up

                    $scope.$watch("forms.hasValidated", function () { 
                        check();
                    });
                    var isValidated = function () {
                        return $scope.forms.hasValidated;
                    };
                    var check = function() {
                        if (isValidated() === true) {
                            if (!$scope.forms.baselineForm[formInputName].$valid) {
                                $element.addClass("error");
                            }
                        }
                    };
                    check();


                    var watch = "forms.baselineForm." + formInputName +".$valid";
                    $scope.$watch(watch, function (value) { 
                        if (isValidated() === true) {
                            if (value === true) {
                                $element.removeClass("error");
                            } if (value === false && $scope.hasValidated) { // hasValidated is set to true when the submit button is clicked;
                                $element.addClass("error");
                            }
                        }
                    });
                }
            }]
        };
    }]);
});