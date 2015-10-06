'use strict';

/* Filters */

angular.module('app.filters', []).
    filter('encodeURIComponent', function () {
        return window.encodeURIComponent;
    });