'use strict';
/* Controllers */
angular.module('app.controllers', ['ngRoute', 'ngGrid', 'ngMessages', 'toaster', 'angularSpinner'])
    .controller('AppController', ['$scope', '$rootScope', '$location', 'Authentication', 'toaster', 'AuthenticationDialog', 'FaultHandler',
        function ($scope, $rootScope, $location, Authentication, toaster, AuthenticationDialog, FaultHandler) {
            $scope.signInSignOut = function () {
                if ($rootScope.loggedInUser != null) {
                    Win
                } else {
                    AuthenticationDialog.open();
                }
            }
            $scope.initState = function () {
                updateAuthentication();
            }
            $scope.executeTest = function () {
                console.info("execute test is called");
            }
            $scope.$on('handleBroadcast', function () {
                updateAuthentication();
            });
            function updateAuthentication() {
                var label = "Sign in";
                if ($rootScope.loggedInUser != null) {
                    label = "Sign out " + $rootScope.loggedInUser.name;
                }
                $scope.signInText = label;
            }
        }
    ])
    .controller('HeaderController', ['$scope', '$location', 'FaultHandler',
        function ($scope, $location, FaultHandler) {
            $scope.isActive = function (viewLocation) {
                return viewLocation === $location.path();
            };
        }
    ])
    .controller('LoginController', ['$scope', '$rootScope', '$http', 'Authentication', 'toaster', 'FaultHandler',
        function ($scope, $rootScope, $http, Authentication, toaster, FaultHandler) {
            $scope.login = function () {
                gapi.auth.authorize(
                    {
                        client_id: "495779795698-vbebu2hji4r9eelps3hjsshafhgusskk.apps.googleusercontent.com",
                        scope: "https://www.googleapis.com/auth/userinfo.profile",
                        immediate: false
                    },
                    authorizeCallback);
            }
            function authorizeCallback(resp) {
                $http(
                    {
                        method: 'GET',
                        url: 'https://www.googleapis.com/userinfo/v2/me?access_token=' + resp.access_token
                    }).
                    success(function (data, status, headers, config) {
                        console.info("request for user info succeeded");
                        $rootScope.loggedInUser = data;
                        $rootScope.$broadcast('handleBroadcast');
                        toaster.pop('success', "Sign-in", "You are now signed in to KnackedUp");
                    }).
                    error(function (data, status, headers, config) {
                        console.error("request for user info failed. Reason: " + data);
                        toaster.pop('error', "Sign-in failed", "An error has taken place");
                    });
            }
        }
    ])
    .controller('HomeController', ['$scope', '$rootScope', '$location', 'toaster', 'FaultHandler',
        function ($scope, $rootScope, $location, toaster, FaultHandler) {
        }
    ])
    .controller("QuickieListController", ['$scope', '$routeParams', '$location', 'toaster', 'usSpinnerService', 'FaultHandler',
        function ($scope, $routeParams, $location, toaster, usSpinnerService, FaultHandler) {
            $scope.clear = function () {
                $scope.term = "";
                setTimeout(function () {
                    $scope.$apply(function () {
                        $scope.list();
                    });
                }, 500);
            }
            $scope.init = function () {
                $scope.filterOptions = {
                    filterText: "",
                    useExternalFilter: true
                };
                $scope.totalServerItems = 0;
                $scope.pagingOptions = {
                    pageSizes: [250, 500, 1000],
                    pageSize: 250,
                    currentPage: 1
                };
                $scope.setPagingData = function (data, page, pageSize) {
                    var pagedData = null;
                    if (data && data.items) {
                        pagedData = data.items.slice((page - 1) * pageSize, page * pageSize);
                    }
                    $scope.dataProvider = pagedData;
                    $scope.totalServerItems = data.length;
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                };
                $scope.$watch('pagingOptions', function (newVal, oldVal) {
                    if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
                    }
                }, true);
                $scope.$watch('filterOptions', function (newVal, oldVal) {
                    if (newVal !== oldVal) {
                        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage, $scope.filterOptions.filterText);
                    }
                }, true);
                $scope.selectedRows = [];
                $scope.gridOptions = {
                    data: 'dataProvider',
                    enablePaging: false,
                    showFooter: false,
                    multiSelect: false,
                    selectedItems: $scope.selectedRows,
                    totalServerItems: 'totalServerItems',
                    pagingOptions: $scope.pagingOptions,
                    filterOptions: $scope.filterOptions,
                    jqueryUITheme: true,
                    enablePinning: false,
                    columnDefs: [
                        {
                            field: 'quickieName',
                            displayName: 'Quickie'
                        }
                    ]
                };
                $scope.getPagedDataAsync = function (pageSize, page, searchText) {
                    var data;
                    if (searchText) {
                        var ft = searchText.toLowerCase();
                        usSpinnerService.spin('spinner-1');
                        gapi.client.knackedupapp.quickie.search(ft).execute(function (response) {
                            if (response.hasOwnProperty("error")) {
                                FaultHandler.flash(response);
                            }
                            else {
                                data = response.filter(function (item) {
                                    return JSON.stringify(item).toLowerCase().indexOf(ft) != -1;
                                });
                                $scope.setPagingData(data, page, pageSize);
                            }
                        }, function (response) {
                            FaultHandler.flash(response);
                        });
                    }
                    else {
                        usSpinnerService.spin('spinner-1');
                        gapi.client.knackedupapp.quickie.list().execute(function (response) {
                            if (response.hasOwnProperty("error")) {
                                FaultHandler.flash(response);
                            }
                            else {
                                $scope.setPagingData(response, page, pageSize);
                                usSpinnerService.stop('spinner-1');
                            }
                        }, function (response) {
                            FaultHandler.flash(response);
                        });
                    }
                };
                $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
            };
            $scope.addProposition = function () {
                $location.path("/quickie/new");
            }
            $scope.goToApply = function () {
                var quickie = $scope.selectedRows[0];
                if (quickie == null) {
                    toaster.pop("error", "No quickie selected", "Please select a quickie to open");
                }
                else {
                    $location.path("/quickie/" + quickie.quickieId + "/proposition/new");
                }
            }
            $scope.openQuickie = function () {
                var quickie = $scope.selectedRows[0];
                console.info("quickie = " + quickie);
                if (quickie == null) {
                    toaster.pop("error", "No quickie selected", "Please select a quickie to open");
                }
                else {
                    $location.path("/quickie/" + quickie.quickieId + "/details");
                }
            };
            $scope.search = function () {
                var term = searchInput.value;
                if (term) {
                    $scope.dataProvider = [];
                    usSpinnerService.spin('spinner-1');
                    gapi.client.knackedupapp.quickie.search(
                        {
                            term: term
                        }).execute(function (response) {
                            if (response.hasOwnProperty("error")) {
                                FaultHandler.flash(response);
                            }
                            else {
                                setTimeout(function () {
                                    $scope.$apply(function () {
                                        $scope.dataProvider = response.items;
                                        usSpinnerService.stop('spinner-1');
                                    });
                                }, 500);
                            }
                        }, function (response) {
                            FaultHandler.flash(response);
                        });
                } else {
                    toaster.pop("info", "No Search Term", "Please enter a search term");
                }
            }
            $scope.list = function () {
                usSpinnerService.spin('spinner-1');
                gapi.client.knackedupapp.quickie.list().execute(function (response) {
                    if (response.hasOwnProperty("error")) {
                        FaultHandler.flash(response);
                    }
                    else {
                        setTimeout(function () {
                            $scope.$apply(function () {
                                $scope.dataProvider = response.items;
                                usSpinnerService.stop('spinner-1');
                            });
                        }, 500);
                    }
                }, function (response) {
                    FaultHandler.flash(response);
                });
            }
        }
    ])
    .controller('QuickieEditController', ['$scope', '$rootScope', '$routeParams', '$location', 'AuthenticationDialog', 'toaster', 'usSpinnerService', 'FaultHandler',
        function ($scope, $rootScope, $routeParams, $location, AuthenticationDialog, toaster, usSpinnerService, FaultHandler) {
            $scope.addProposition = function () {
                $location.path("/quickie/edit");
            };
            var callBack = function () {
                $scope.init();
            }
            $scope.init = function () {
                if ($rootScope.loggedInUser == undefined) {
                    AuthenticationDialog.open(callBack);
                    return;
                }
                var quickieId = $routeParams.quickieId;
                if (quickieId != null) {
                    usSpinnerService.spin('spinner-1');
                    gapi.client.knackedupapp.quickie.get(
                        {
                            quickieId: quickieId
                        }).execute(function (response) {
                            if (response.hasOwnProperty("error")) {
                                FaultHandler.flash(response);
                            }
                            else {
                                setTimeout(function () {
                                    $scope.$apply(function () {
                                        $scope.data = response;
                                        usSpinnerService.stop('spinner-1');
                                    });
                                }, 500);
                            }
                        }, function (response) {
                            FaultHandler.flash(response);
                        });
                }
            };
            $scope.delete = function () {
                if ($routeParams.quickieId == null) {
                    $location.path("/");
                    toaster.pop("success", "Success", "The quickie is discarded");
                } else {
                    usSpinnerService.spin('spinner-1');
                    gapi.client.knackedupapp.quickie.delete({quickieId: $routeParams.quickieId}).execute(function (response) {
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            toaster.pop("success", "Success", "The quickie is discarded");
                            $location.path("/");
                            usSpinnerService.stop('spinner-1');
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
                }
            }
            $scope.save = function () {
                $scope.submitted = true;
                if ($scope.form.$valid == false) {
                    toaster.pop("error", "Form Invalid", "Please correct the errors and try again");
                    return;
                }
                var quickie = {
                    "quickieName": $scope.data.quickieName,
                    "profileURL": $scope.data.profileURL,
                    "termsAccepted": $scope.data.termsAccepted,
                    "description": $scope.data.description,
                    "organization": $scope.data.organization
                };
                var hasAcceptedTOS = $scope.data.termsAccepted;
                if (hasAcceptedTOS == false) {
                    toaster.pop("error", "Terms not Accepted", "You did not accept the Terms of Service");
                    return;
                }
                if ($routeParams.quickieId == null) {
                    usSpinnerService.spin('spinner-1');
                    gapi.client.knackedupapp.quickie.insert(quickie).execute(function (response) {
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            $location.path("/");
                            setTimeout(function () {
                                $scope.$apply(function () {
                                    toaster.pop("success", "Success", "The quickie was added");
                                    usSpinnerService.stop('spinner-1');
                                })
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
                }
                else {
                    usSpinnerService.spin('spinner-1');
                    quickie["quickieId"] = $routeParams.quickieId;
                    gapi.client.knackedupapp.quickie.update(quickie).execute(function (response) {
                        console.log(response.hasOwnProperty("error"));
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            setTimeout(function () {
                                $scope.$apply(function () {
                                    toaster.pop("success", "Success", "The quickie was saved");
                                    usSpinnerService.stop('spinner-1');
                                })
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
                }
            };
        }
    ])
    .controller('QuickieDetailsController', ['$scope', '$rootScope', '$location', '$routeParams', 'toaster', 'AuthenticationDialog', 'usSpinnerService', 'FaultHandler',
        function ($scope, $rootScope, $location, $routeParams, toaster, AuthenticationDialog, usSpinnerService, FaultHandler) {
            $scope.doConfirm = function () {
                bootbox.confirm("Are you sure you want to Flag this Quickie?", function (result) {
                    if (result) {
                        var flag = {
                            "quickieId": $routeParams.quickieId
                        };
                        usSpinnerService.spin('spinner-1');
                        gapi.client.knackedupapp.flag.insert(flag).execute(
                            function (response) {
                                if (response.hasOwnProperty("error")) {
                                    FaultHandler.flash(response);
                                }
                                else {
                                    setTimeout(function () {
                                        $scope.$apply(function () {
                                            $location.path("/");
                                            toaster.pop("success", "Quickie Flagged", "Quickie flagged as inappropriate");
                                            usSpinnerService.stop('spinner-1');
                                        })
                                    }, 500);
                                }
                            }, function (response) {
                                FaultHandler.flash(response);
                            });
                    } else {
                    }
                });
            }
            $scope.goBack = function () {
                history.back();
            }
            $scope.goToApply = function () {
                var quickieId = $routeParams.quickieId;
                if (quickieId == null) {
                    toaster.pop("error", "Invalid Quickie ID", "The quickie ID specified is not valid");
                    return;
                }
                $location.path("/quickie/" + quickieId + "/proposition/new");
            }
            $scope.init = function () {
                var quickieId = $routeParams.quickieId;
                if (quickieId == null) {
                    toaster.pop("error", "Invalid quickie ID", "The quickie ID specified is not valid");
                    return;
                }
                $scope.form = {};
                usSpinnerService.spin('spinner-1');
                gapi.client.knackedupapp.quickie.get(
                    {
                        quickieId: quickieId
                    }).execute(
                    function (response) {
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            var t = setTimeout(function () {
                                $scope.$apply(function () {
                                    $scope.data = response;
                                    usSpinnerService.stop('spinner-1');
                                })
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
            }
        }
    ])
    .controller('KnackController', ['$scope',
        function ($scope) {
        }
    ])
    .controller('TermsController', ['$scope',
        function ($scope) {
        }
    ])
    .controller('AboutController', ['$scope',
        function ($scope) {
        }
    ])
    .controller('PropositionEditController', ['$scope', '$rootScope', '$location', '$routeParams', 'toaster', 'usSpinnerService', 'AuthenticationDialog', 'FaultHandler',
        function ($scope, $rootScope, $location, $routeParams, toaster, usSpinnerService, AuthenticationDialog, FaultHandler) {
            var callBack = function () {
                $scope.init();
            }
            $scope.init = function () {
                if ($rootScope.loggedInUser == undefined) {
                    AuthenticationDialog.open(callBack);
                    return;
                }
            }
            $scope.save = function () {
                $scope.submitted = true;
                if ($scope.form.$valid == false) {
                    toaster.pop("error", "Form Invalid", "Please correct the errors and try again");
                    return;
                }
                console.info("termsAccepted : " + $scope.data.termsAccepted);
                var quickieId = $routeParams.quickieId;
                if (quickieId == 'undefined') {
                    toaster.pop("error", "Invalid quickie ID", "The quickie ID specified is not valid");
                    return;
                }
                var hasAcceptedTOS = $scope.data.termsAccepted;
                if (hasAcceptedTOS == false) {
                    toaster.pop("error", "Terms not Accepted", "You did not accept the Terms of Service");
                    return;
                }
                var proposition = {
                    "quickieId": $routeParams.quickieId,
                    "title": $scope.data.title,
                    "profileURL": $scope.data.profileURL,
                    "knack": $scope.data.knack,
                    "termsAccepted": $scope.data.termsAccepted
                };
                usSpinnerService.spin('spinner-1');
                gapi.client.knackedupapp.proposition.insert(proposition).execute(function (response) {
                    if (response.hasOwnProperty("error")) {
                        FaultHandler.flash(response);
                    }
                    else {
                        usSpinnerService.stop('spinner-1');
                        $location.path("/");
                        setTimeout(function () {
                            $scope.$apply(function () {
                                toaster.pop("success", "Success", "Your application is sent");
                            })
                        }, 500);
                    }
                }, function (response) {
                    FaultHandler.flash(response);
                });
            };
        }
    ])
    .controller('MyQuickieListController', ['$scope', '$rootScope', '$location', 'toaster', 'usSpinnerService', 'AuthenticationDialog', 'FaultHandler',
        function ($scope, $rootScope, $location, toaster, usSpinnerService, AuthenticationDialog, FaultHandler) {
            $scope.selectedRows = [];
            $scope.gridOptions = {
                data: 'dataProvider',
                enablePaging: false,
                showFooter: false,
                multiSelect: false,
                selectedItems: $scope.selectedRows,
                enablePinning: false,
                columnDefs: [
                    {
                        field: 'quickieName',
                        displayName: 'Quickie'
                    }
                ]
            };
            $scope.goToPropositions = function () {
                var quickie = $scope.selectedRows[0];
                if (quickie == null) {
                    toaster.pop("error", "No quickie selected", "Please select a quickie to show the suitors");
                }
                else {
                    $location.path("/quickie/" + quickie.quickieId + "/proposition/list");
                }
            }
            $scope.goToAddQuickie = function () {
                $location.path("/quickie/new");
            }
            $scope.edit = function () {
                var quickie = $scope.selectedRows[0];
                if (quickie == null) {
                    toaster.pop("error", "No Quickie Selected", "Please select a quickie to edit");
                }
                else {
                    $location.path("/quickie/" + quickie.quickieId + "/edit");
                }
            }
            var callBack = function () {
                $scope.init();
            }
            $scope.init = function () {
                if ($rootScope.loggedInUser == undefined) {
                    AuthenticationDialog.open(callBack);
                    return;
                }
                usSpinnerService.spin('spinner-1');
                gapi.client.knackedupapp.user.quickie.list(
                    {}).execute(function (response) {
                        if (response.error) {
                            toaster.pop("error", "Error", response.error.message);
                            usSpinnerService.stop('spinner-1');
                        }
                        else {
                            setTimeout(function () {
                                $scope.$apply(function () {
                                    if (response.items) {
                                        $scope.dataProvider = response.items;
                                    } else {
                                        $scope.dataProvider = [];
                                    }
                                    usSpinnerService.stop('spinner-1');
                                });
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
            }
        }
    ])
    .controller('PropositionListController', ['$scope', '$rootScope', '$location', '$routeParams', 'toaster', 'usSpinnerService', 'FaultHandler',
        function ($scope, $rootScope, $location, $routeParams, toaster, usSpinnerService, FaultHandler) {
            $scope.selectedRows = [];
            $scope.gridOptions = {
                data: 'dataProvider',
                enablePaging: false,
                showFooter: false,
                multiSelect: false,
                selectedItems: $scope.selectedRows,
                enablePinning: false,
                columnDefs: [
                    {
                        field: 'title',
                        displayName: 'Proposition'
                    }
                ]
            };
            $scope.init = function () {
                console.info("PropositionListController : init");
                var quickieId = $routeParams.quickieId;
                if (quickieId == 'undefined') {
                    toaster.pop("error", "Invalid Quickie ID", "The quickie ID specified is not valid");
                    return;
                }
                usSpinnerService.spin('spinner-1');
                gapi.client.knackedupapp.proposition.list(
                    {"quickieId": quickieId}).execute(function (response) {
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            setTimeout(function () {
                                $scope.$apply(function () {
                                    if (response.items) {
                                        $scope.dataProvider = response.items;
                                    } else {
                                        $scope.dataProvider = [];
                                    }
                                    usSpinnerService.stop('spinner-1');
                                });
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
            }
            $scope.goToAddProposition = function () {
                $location.path("/quickie/" + $routeParams.quickieId + "/proposition/new");
            }
            $scope.getPropositionDetails = function () {
                var selection = $scope.selectedRows[0];
                if (selection == null) {
                    toaster.pop("error", "No Proposition Selected", "Please select a proposition");
                }
                else {
                    $location.path("/user/" + selection.suitorId + "/proposition/" + selection.propositionId);
                }
            }
        }
    ])
    .controller('PropositionDetailsController', ['$scope', '$rootScope', '$location', '$routeParams', 'toaster', 'usSpinnerService', 'AuthenticationDialog', 'FaultHandler',
        function ($scope, $rootScope, $location, $routeParams, toaster, usSpinnerService, AuthenticationDialog, FaultHandler) {
            var contactDetailsCallBack = function () {
                $scope.getContactDetails();
            }
            $scope.getContactDetails = function () {
                if ($rootScope.loggedInUser == null) {
                    toaster.pop("error", "Not logged in", "You must be logged in to see the suitor");
                    setTimeout(function () {
                        $scope.$apply(function () {
                            AuthenticationDialog.open(contactDetailsCallBack);
                        });
                    }, 500);
                    return;
                }
                gapi.client.knackedupapp.user.get(
                    {'googleId': $scope.propositionData.suitorId}).execute(function (response) {
                        if (response.hasOwnProperty("error")) {
                            FaultHandler.flash(response);
                        }
                        else {
                            setTimeout(function () {
                                $scope.$apply(function () {
                                    $scope.userData = response;
                                    usSpinnerService.stop('spinner-1');
                                });
                            }, 500);
                        }
                    }, function (response) {
                        FaultHandler.flash(response);
                    });
            }
            $scope.init = function () {
                console.info("PropositionDetailsController : init");
                var propositionId = $routeParams.propositionId;
                if (propositionId == 'undefined') {
                    toaster.pop("error", "Invalid Quickie ID", "The ID specified is not valid");
                } else {
                    usSpinnerService.spin('spinner-1');
                    gapi.client.knackedupapp.proposition.get(
                        {'propositionId': propositionId}).execute(function (response) {
                            if (response.hasOwnProperty("error")) {
                                FaultHandler.flash(response);
                            }
                            else {
                                setTimeout(function () {
                                    $scope.$apply(function () {
                                        $scope.propositionData = response;
                                        usSpinnerService.stop('spinner-1');
                                    });
                                }, 500);
                            }
                        }, function (response) {
                            FaultHandler.flash(response);
                        });
                }
            }
        }
    ])