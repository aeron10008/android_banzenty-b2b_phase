package com.restart.banzenty.utils

class Constants {
    companion object {
        //const val BASE_URL = "https://banzenty.restart-technology.com/api/"
        const val BASE_URL = "https://banzenty.com/api/"
        //const val BASE_URL = "https://banzenty-testing.restart-technology.com/api/"
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing
        const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
        const val PAGINATION_PAGE_SIZE = 50
        const val DATABASE_VERSION = 5
        const val DATABASE_NAME = "com.restart.banzenty_db"
        const val NETWORK_TIMEOUT = 12000L
        const val UNABLE_TO_RESOLVE_HOST = "Unable to resolve host"
        const val PAGINATION_DONE = "Invalid page"
        const val ERROR_UNKNOWN = "Unknown error"
        const val ERROR_CHECK_NETWORK_CONNECTION = "Check network connection."
        const val UNABLE_TODO_OPERATION_WO_INTERNET = "No Internet"
        const val NEW_NOTIFICATION_ACTION = "NEW_NOTIFICATION_ACTION"
        const val FAWRY_PAYMENT_URL = "https://atfawry.fawrystaging.com/"
        const val MERCHANTCODE = "770000014974"
        const val MERCHANTSECRETCODE = "e9586f09-266f-4b86-9503-c2b55946e94b"

    }
}