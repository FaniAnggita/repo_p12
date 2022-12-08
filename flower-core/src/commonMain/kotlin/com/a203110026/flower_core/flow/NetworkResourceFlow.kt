/*
 *  Copyright (C) 2022 Rajesh Hadiya
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.a203110026.flower_core.flow

import com.a203110026.flower_core.ApiEmptyResponse
import com.a203110026.flower_core.ApiErrorResponse
import com.a203110026.flower_core.ApiResponse
import com.a203110026.flower_core.ApiSuccessResponse
import com.a203110026.flower_core.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Make a network request and emit the response. Additionally, takes an action to perform if a network request fails.
 *
 * Difference between this function and [networkResource] is that, [networkResource] emits the data only once, while this function will emit [Flow] of data.
 * Moreover, the function called in [makeNetworkRequest] must NOT be a `suspend` function.
 *
 * @author Rajesh Hadiya
 * @param makeNetworkRequest - A function to make network request
 * @param onNetworkRequestFailed - An action to perform when a network request fails
 * @return [Flow] of [REMOTE] type
 */
inline fun <REMOTE> networkResourceFlow(
    crossinline makeNetworkRequest: () -> Flow<ApiResponse<REMOTE>>,
    crossinline onNetworkRequestFailed: (errorMessage: String, httpStatusCode: Int) -> Unit = { _: String, _: Int -> }
) = flow<Resource<REMOTE>> {
    emit(Resource.loading(data = null))

    makeNetworkRequest().collect { apiResponse ->
        when (apiResponse) {
            is ApiSuccessResponse -> {
                apiResponse.body?.let {
                    emit(Resource.success(data = it))
                }
            }

            is ApiErrorResponse -> {
                onNetworkRequestFailed(
                    apiResponse.errorMessage,
                    apiResponse.httpStatusCode
                )
                emit(
                    Resource.error(
                        errorMessage = apiResponse.errorMessage,
                        httpStatusCode = apiResponse.httpStatusCode,
                        data = null
                    )
                )
            }

            is ApiEmptyResponse -> {
                emit(Resource.emptySuccess())
            }
        }
    }
}
