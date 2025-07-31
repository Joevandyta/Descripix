package com.jovan.descripix.data

//
//class FakeDescripixRepository: IDescripixRepository {
//
//    private lateinit var remoteDataSource: FakeRemoteDataSource
//    private lateinit var localDataSource: FakeLocalDataSource
//
//    @Before
//    fun setup(){
//        remoteDataSource = FakeRemoteDataSource()
//        localDataSource = FakeLocalDataSource()
//    }
//
//    override fun getSession(context: Context, isConnected: Boolean): Flow<SessionData> =
//        localDataSource.getSession().map { session ->
//            var currentSession = session
//            if (currentSession.token.isBlank() || currentSession.refreshToken.isBlank()) {
//                localDataSource.logout()
//                return@map currentSession
//            }
//            if (isConnected) {
//                try {
//                    val tokenVerify =
//                        handleApiException { remoteDataSource.tokenVerify(currentSession.token) }
//                    if (!tokenVerify.status && !tokenVerify.message.toString()
//                            .contains("Connection Timeout")
//                    ) {
//                        val refreshed =
//                            handleApiException { remoteDataSource.refreshToken(currentSession.refreshToken) }
//                        if (refreshed.status) {
//                            currentSession = SessionData(
//                                refreshToken = currentSession.refreshToken,
//                                token = refreshed.data!!.access,
//                                isLogin = true
//                            )
//                            localDataSource.saveSession(currentSession)
//                        } else {
//                            localDataSource.logout()
//                            localDataSource.deleteUser()
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("REPO", e.message.toString())
//                }
//            }
//            currentSession
//
//        }.catch {
//            emit(SessionData.empty())
//        }
//
//
//    override suspend fun logout(
//        refresh: String,
//        context: Context
//    ): ApiResponse<Unit> {
//        val response = remoteDataSource.logout(refresh, context)
//        localDataSource.deleteUser()
//        localDataSource.deleteAllCaption()
//        localDataSource.logout()
//
//        return response
//    }
//
//    override suspend fun saveLanguage(language: Language) {
//        return localDataSource.saveLanguage(language)
//    }
//
//    override fun getLanguage(): Flow<Language> {
//        return localDataSource.getLanguage()
//    }
//
//    override suspend fun login(
//        googleId: String,
//        context: Context
//    ): ApiResponse<LoginResponse> {
//        val response = remoteDataSource.googleLogin(googleId)
//        if (response.status && response.data != null) {
//            localDataSource.saveSession(
//                SessionData(
//                    refreshToken = response.data!!.refresh!!,
//                    token = response.data!!.access,
//                )
//            )
//        }
//        return response
//    }
//
//    override fun isConnected(): Flow<Boolean> = remoteDataSource.isConnected()
//
//    override suspend fun getAllCaptions(
//        isConnected: Boolean,
//        token: String,
//        context: Context
//    ): Flow<List<CaptionEntity>> = flow {
//        var currentCaption = localDataSource.getAllCaption().first()
//        if (isConnected) {
//            try {
//                val apiResponse = remoteDataSource.getCaptionList(token, context)
//                if (apiResponse.status) {
//                    val captionsFromServer = apiResponse.data
//                    try {
//                        val entities = mutableListOf<CaptionEntity>()
//                        captionsFromServer?.forEach { serverCaption ->
//                            try {
//                                val imageFile = withContext(Dispatchers.IO) {
//                                    downloadImageToFile(context, serverCaption.image)
//                                        ?.resizeIfTooLarge()
//                                        ?.reduceFileSize()
//                                }
//                                if (imageFile != null) {
//                                    entities.add(
//                                        CaptionEntity(
//                                            id = serverCaption.id,
//                                            caption = serverCaption.caption,
//                                            author = null,
//                                            date = null,
//                                            location = null,
//                                            device = null,
//                                            model = null,
//                                            image = imageFile.absolutePath,
//                                        )
//                                    )
//                                }
//                            } catch (_: Exception) {
//                            }
//                        }
//                        currentCaption = entities
//
//                    } catch (e: Exception) {
//                        // Keep currentCaption as is (local data)
//                    }
//                }
//            } catch (e: Exception) {
//                // Keep currentCaption as is (local data)
//            }
//            val localCaptionBeforeProcessing = localDataSource.getAllCaption().first()
//            if (currentCaption != localCaptionBeforeProcessing) {
//                try {
//                    localDataSource.deleteAllCaption()
//                    localDataSource.insertCaption(currentCaption)
//                    emit(localDataSource.getAllCaption().first())
//                } catch (e: Exception) {
//                    emit(currentCaption)
//                }
//            } else {
//                emit(currentCaption)
//            }
//        } else {
//            emit(currentCaption)
//        }
//    }.distinctUntilChanged()
//
//
//    override suspend fun getUserDetail(
//        isConnected: Boolean,
//        refreshToken: String,
//        token: String,
//        context: Context
//    ): Flow<UserEntity> = flow {
//        val localUser = localDataSource.getUser(refreshToken).first()
//        if (isConnected) {
//            val response = remoteDataSource.getUserDetail(token, context)
//            val onlineUser = response.data
//
//            if (response.status && onlineUser != null) {
//
//                if (localUser != null) localDataSource.deleteUser()
//                val userEntity = UserEntity(
//                    id = refreshToken,
//                    username = response.data!!.username,
//                    email = response.data!!.email,
//                    gender = response.data!!.gender,
//                    birthDate = response.data!!.birthDate,
//                    aboutMe = response.data!!.aboutMe,
//                    profileImg = response.data!!.profileImg,
//                )
//                localDataSource.insertUser(
//                    userEntity
//                )
//                if (localUser != userEntity)
//                    emit(userEntity)
//            }
//        }
//        val newLocalUser = localDataSource.getUser(refreshToken).first()
//        if (newLocalUser != null) {
//            emit(newLocalUser)
//        }
//    }
//
//
//    override suspend fun updateUserDetail(
//        userRequest: UserRequest,
//        token: String,
//        context: Context
//    ): ApiResponse<Unit> {
//        return remoteDataSource.updateUserDetail(userRequest, token, context)
//    }
//
//    //Remote Caption
//    override suspend fun saveCaption(
//        captionRequest: CaptionRequest,
//        token: String,
//        context: Context
//    ): ApiResponse<CaptionDataResponse> =
//        remoteDataSource.saveCaption(captionRequest, token, context)
//
//    override suspend fun deleteCaption(
//        id: Int,
//        token: String,
//        context: Context
//    ): ApiResponse<Unit> =
//        remoteDataSource.deleteCaption(id, token, context)
//
//    override suspend fun editCaption(
//        id: Int,
//        captionRequest: CaptionRequest,
//        token: String,
//        context: Context
//    ): ApiResponse<Unit> =
//        remoteDataSource.editCaption(id, captionRequest, token, context)
//
//    override suspend fun generateCaption(
//        metadata: JSONObject,
//        image: Uri,
//        context: Context
//    ) =
//        remoteDataSource.generateCaption(metadata, image, context)
//
//    override suspend fun getCaptionDetails(
//        id: Int,
//        token: String,
//        context: Context
//    ): ApiResponse<CaptionDataResponse> {
//        val response = remoteDataSource.getCaptionDetails(id, token, context)
//        var finalResponse = response
//        if (response.status && response.data?.date == null) {
//            finalResponse = response.copy(data = response.data?.copy(date = ""))
//        }
//        return finalResponse
//    }
//}