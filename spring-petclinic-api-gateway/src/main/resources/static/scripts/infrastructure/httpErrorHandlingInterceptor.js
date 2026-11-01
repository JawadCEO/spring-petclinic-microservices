'use strict';

/**
 * Global HTTP errors handler.
 */
angular.module('infrastructure')
    .factory('HttpErrorHandlingInterceptor', ['$q', function ($q) {
        return {
            responseError: function (response) {
                var error = response.data;
                // Only validation errors list their fields; the caller handles any other failure
                if (!error || !error.errors) {
                    return $q.reject(response);
                }
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
                return response;
            }
        }
    }]);
