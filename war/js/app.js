'use strict';
// Declare app level module which depends on filters, and services
var app = angular.module(
    'app',
    ['ngRoute', 'app.filters', 'gapi.client.knackedupapp',
        'app.directives', 'app.controllers']).constant("API_KEY",
    "DEFINE_API_KEY").config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/quickie/new', {
            title: 'Add Quickie',
            templateUrl: 'partials/quickie-edit.html'
        });
        $routeProvider.when('/quickie/:quickieId/edit', {
            title: 'Edit Quickie',
            templateUrl: 'partials/quickie-edit.html'
        });
        $routeProvider.when('/quickie/:quickieId/details', {
            title: 'Quickie Details',
            templateUrl: 'partials/quickie-details.html'
        });
        $routeProvider.when('/quickie/:quickieId/proposition/new', {
            title: 'Make Your Proposition',
            templateUrl: 'partials/proposition-edit.html'
        });
        $routeProvider.when('/user/quickie/list', {
            title: 'My Quickies',
            templateUrl: 'partials/user-quickie-list.html'
        });
        $routeProvider.when('/quickie/:quickieId/proposition/list', {
            title: 'Propositions',
            templateUrl: 'partials/proposition-list.html'
        });
        $routeProvider.when('/user/:userId/proposition/:propositionId', {
            title: 'Proposition Details',
            templateUrl: 'partials/proposition-details.html'
        });
        $routeProvider.when('/about', {
            title: 'About',
            templateUrl: 'partials/about.html'
        });
        $routeProvider.when('/how-it-works', {
            title: 'How it Works',
            templateUrl: 'partials/how-it-works.html'
        });
        $routeProvider.when('/terms', {
            title: 'Terms',
            templateUrl: 'partials/terms.html'
        });
        $routeProvider.when('/', {
            title: 'Find Quickies',
            templateUrl: 'partials/quickie-list.html'
        });
    }]);
app.run([
    '$location',
    '$rootScope',
    'toaster',
    'AuthenticationDialog',
    function ($location, $rootScope, toaster, AuthenticationDialog) {
        $rootScope.$on('$routeChangeSuccess', function (event, current,
                                                        previous) {
            if (current) {
                $rootScope.title = current.$$route.title;
            }
        });
    }]);
app.factory('$exceptionHandler', function () {
    return function (exception, cause) {
        exception.message += ' (caused by "' + cause + '")';
        console.error(exception);
        throw exception;
    };
});
app.factory("FaultHandler", function (toaster, usSpinnerService) {
    return {
        flash: function (response) {
            setTimeout(function () {
                console.error(response.error.message);
                toaster.pop("error", "Error", response.error.message);
                usSpinnerService.stop('spinner-1');
            }, 500);
        }
    };
});
app.factory("Quickie", function () {
    var quickies = [];
    for (var i = 0; i < 50; i++) {
        quickies.push({
            id: i,
            quickie: "quickie " + i,
            description: "description " + i
        });
    }
    return {
        get: function (offset, limit) {
            return quickies.slice(offset, offset + limit);
        },
        total: function () {
            return quickies.length;
        }
    };
});
app.factory('Authentication', function ($rootScope) {
    var sharedService = {};
    sharedService.message = '';
    sharedService.prepForBroadcast = function (msg) {
        this.message = msg;
        this.broadcastItem();
    };
    sharedService.broadcastItem = function () {
        $rootScope.$broadcast('handleBroadcast');
    };
    return sharedService;
});
app
    .factory(
    'AuthenticationDialog',
    function ($modal, $location, $log, Authentication, toaster,
              usSpinnerService) {
        var service = {};
        var callBack;
        service.open = function (responder) {
            callBack = responder;
            var modalInstance = $modal.open({
                templateUrl: 'partials/unauth-dialog.html',
                controller: ModalInstanceCtrl,
                backdrop: "static",
                keyboard: true,
                size: null,
                callBack: callBack
            });
            modalInstance.result.then(function () {
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };
        var ModalInstanceCtrl = function ($modalInstance,
                                          $rootScope, $scope, $http, usSpinnerService) {
            $rootScope.ok = function () {
                gapi.auth
                    .authorize(
                    {
                        client_id: "495779795698-vbebu2hji4r9eelps3hjsshafhgusskk.apps.googleusercontent.com",
                        scope: "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile",
                        immediate: false
                    }, authorizeCallback);
            };
            function authorizeCallback(resp) {
                $http(
                    {
                        method: 'GET',
                        url: 'https://www.googleapis.com/userinfo/v2/me?access_token='
                        + resp.access_token
                    })
                    .success(
                    function (data, status, headers,
                              config) {
                        console
                            .info("request for user info succeeded");
                        console
                            .info("resp.access_token = "
                            + resp.access_token);
                        $rootScope.loggedInUser = data;
                        var access = {
                            "accessToken": resp.access_token
                        };
                        gapi.client.knackedupapp.user
                            .save(access)
                            .execute(
                            function (response) {
                                if (response
                                        .hasOwnProperty("error")) {
                                    toaster
                                        .pop(
                                        "error",
                                        "Error",
                                        response.error.message);
                                    usSpinnerService
                                        .stop('spinner-1');
                                } else {
                                    $rootScope
                                        .$broadcast('handleBroadcast');
                                    setTimeout(
                                        function () {
                                            $modalInstance
                                                .close();
                                            if (callBack) {
                                                callBack
                                                    .apply();
                                                $scope.focusInput = true;
                                            }
                                        },
                                        500);
                                }
                            },
                            function (response) {
                                toaster
                                    .pop(
                                    "error",
                                    "Error",
                                    "An error has occurred");
                            });
                    })
                    .error(
                    function (data, status, headers,
                              config) {
                        console
                            .error("request for user info failed. Reason: "
                            + data);
                        toaster
                            .pop(
                            'error',
                            "Sign-in failed",
                            "An error has taken place");
                    });
            }

            $rootScope.cancel = function () {
                $modalInstance.dismiss('cancel');
                $location.path('/');
            };
        };
        return service;
    });