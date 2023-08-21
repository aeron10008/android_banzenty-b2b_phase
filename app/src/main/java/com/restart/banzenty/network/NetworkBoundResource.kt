package com.restart.banzenty.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.restart.banzenty.utils.Constants.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.restart.banzenty.utils.Constants.Companion.ERROR_UNKNOWN
import com.restart.banzenty.utils.Constants.Companion.NETWORK_TIMEOUT
import com.restart.banzenty.utils.Constants.Companion.TESTING_CACHE_DELAY
import com.restart.banzenty.utils.Constants.Companion.TESTING_NETWORK_DELAY
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.restart.banzenty.utils.Constants.Companion.UNABLE_TO_RESOLVE_HOST
import com.restart.banzenty.utils.MainUtils.Companion.isNetworkError
import com.restart.banzenty.utils.Response
import com.restart.banzenty.utils.ResponseType
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

abstract class NetworkBoundResource<ResponseObject, CacheObject>(
    isNetworkAvailable: Boolean,//is there a network connection?
    isNetworkRequest: Boolean,//is this a network request?
    shouldLoadFromCache: Boolean,//should the cached data loaded firstly?
    shouldCancelIfNoInternet: Boolean//should cancel if there is no internet
) {
    private val TAG = "NetworkBoundResource"

    protected val result = MediatorLiveData<DataState<CacheObject>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())

        if (isNetworkRequest) {
            setValue(DataState.loading(isLoading = true))
        }

        if (shouldLoadFromCache) {
            val dbSource = loadFromCache()

            result.addSource(dbSource) {
                result.removeSource(dbSource)
                setValue(DataState.loading(isLoading = true, cashedData = it))
            }
        }

        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                doNetworkRequest()
            }
            else {
                if (shouldCancelIfNoInternet) {
                    onErrorReturn(
                        UNABLE_TODO_OPERATION_WO_INTERNET,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }
                else{
                    doCacheRequest()
                }
            }
        }
        else {
            doCacheRequest()
        }
    }

    private fun doNetworkRequest() {
        coroutineScope.launch {
            //simulate a network delay for testing
            delay(TESTING_NETWORK_DELAY)
            withContext(Main) {
                //make a network call
                val apiResponse = createCall()
                result.addSource(apiResponse) { response ->
                    result.removeSource(apiResponse)
                    coroutineScope.launch {
                        handleNetworkCall(response)
                    }
                }
            }
        }

        GlobalScope.launch(IO) {
            delay(NETWORK_TIMEOUT)

            if (!job.isCompleted) {
                Log.e(TAG, "NetworkBoundResource: JOB NETWORK TIMEOUT.")
                job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
            }
        }
    }

    private fun doCacheRequest() {
        coroutineScope.launch {
            //fake delay
            delay(TESTING_CACHE_DELAY)

            //View data from cache and return
            createCacheRequest()
        }
    }

    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>) {
        when(response) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }

            is ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
             try {
                 val errorBody = Gson().fromJson(response.errorMessage, GenericResponse::class.java)
                 onErrorReturn(errorBody.message, shouldUseDialog = false, shouldUseToast = true)
             }   catch (e:Exception)  {
                 onErrorReturn(response.errorMessage, shouldUseDialog = true, shouldUseToast = false)
             }
            }

            is ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: Request returned NOTHING (HTTP 204)")
                onErrorReturn("HTTP 204. Returned nothing.", shouldUseDialog = true, shouldUseToast = false)
            }
        }
    }

    fun onCompleteJob(dataState: DataState<CacheObject>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<CacheObject>) {
        result.value = dataState
    }

    private fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var msg = errorMessage
        var responseType: ResponseType = ResponseType.None()

        if (msg == null) {
            msg = ERROR_UNKNOWN
        }
        else if (isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
        }

        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }

        else if (shouldUseDialog) {
            responseType = ResponseType.Dialog()
        }

        // complete job and emit data state
        onCompleteJob(
            DataState.error(
                response = Response(
                    message = msg,
                    responseType = responseType
                )
            )
        )
    }

    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: called")

        job = Job()
        job.invokeOnCompletion {
            if (job.isCancelled) {
                it?.let {
                    //show error dialog
                    onErrorReturn(it.message, shouldUseDialog = false, shouldUseToast = true)
                } ?: onErrorReturn(ERROR_UNKNOWN, shouldUseDialog = false, shouldUseToast = false)
            }
            else if (job.isCompleted) {
                Log.d(TAG, "invoke: Job has been completed")
                //Do Nothing, should be handled already
            }
        }
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)
    abstract suspend fun createCacheRequest()
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)
    abstract fun loadFromCache(): LiveData<CacheObject>
    abstract suspend fun updateLocalDb(cacheObject: CacheObject?)
    fun asLiveData() = result as LiveData<DataState<CacheObject>>
}
