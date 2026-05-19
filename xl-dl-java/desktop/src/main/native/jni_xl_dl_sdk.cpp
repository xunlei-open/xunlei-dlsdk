#include "jni_xl_dl_sdk.h"
#include "json.hpp"
#include <string.h>
#include <stdlib.h>
#include <string>

#ifdef  __cplusplus
extern "C" {
#endif

#define check_param_null(param) if (param == nullptr) return XL_DL_ERROR_PARAM_ERROR

#define jstring_to_chars(js) (js == nullptr? nullptr : env->GetStringUTFChars(js, nullptr))
#define release_jstring_chars(js, chars) if (chars != nullptr) env->ReleaseStringUTFChars(js, chars)

#define get_array_length(array) (array == nullptr? 0 : env->GetArrayLength(array))
#define get_array_elements(type, array) (array == nullptr? nullptr : env->Get##type##ArrayElements(array, nullptr))
#define release_array_elements(type, array, items) if (array != nullptr) env->Release##type##ArrayElements(array, items, 0)


using json = nlohmann::json;

// JNI 函数实现

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_init(JNIEnv *env, jclass clazz, jobject joInitParam) {
    check_param_null(joInitParam);
    jclass jcInitParam = env->GetObjectClass(joInitParam);

    auto jsAppId = (jstring)env->GetObjectField(joInitParam, env->GetFieldID(jcInitParam, "appId", "Ljava/lang/String;"));
    auto jsAppVersion = (jstring)env->GetObjectField(joInitParam, env->GetFieldID(jcInitParam, "appVersion", "Ljava/lang/String;"));
    auto jsCfgPath = (jstring)env->GetObjectField(joInitParam, env->GetFieldID(jcInitParam, "cfgPath", "Ljava/lang/String;"));

    xl_dl_init_param param;

    param.app_id = jstring_to_chars(jsAppId);
    param.app_version = jstring_to_chars(jsAppVersion);
    param.cfg_path = jstring_to_chars(jsCfgPath);
    param.save_tasks = (uint8_t)(env->GetIntField(joInitParam, env->GetFieldID(jcInitParam, "saveTasks", "I")) ? 1 : 0);

    int result = xl_dl_init(&param);

    release_jstring_chars(jsAppId, param.app_id);
    release_jstring_chars(jsAppVersion, param.app_version);
    release_jstring_chars(jsCfgPath, param.cfg_path);

    env->DeleteLocalRef(jsAppId);
    env->DeleteLocalRef(jsAppVersion);
    env->DeleteLocalRef(jsCfgPath);

    env->DeleteLocalRef(jcInitParam);
    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_uninit(JNIEnv *env, jclass clazz) {
    return xl_dl_uninit();
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_login(JNIEnv *env, jclass clazz, jstring jsLoginToken, jobject sessionIdGetter) {
    check_param_null(sessionIdGetter);
    const char *loginToken = jstring_to_chars(jsLoginToken);

    char sessionID[XL_DL_MAX_SESSION_ID_LEN]{};
    int result = xl_dl_login(loginToken, sessionID);
    if (result == XL_DL_ERROR_SUCCESS) {
        jstring jsSessionID = env->NewStringUTF(sessionID);
        env->SetObjectField(sessionIdGetter, env->GetFieldID(env->GetObjectClass(sessionIdGetter), "value", "Ljava/lang/String;"), jsSessionID);
    }

    release_jstring_chars(jsLoginToken, loginToken);

    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getUnfinishedTask(JNIEnv *env, jclass clazz, jlongArray joTaskIdArray, jobject countGetter) {
    check_param_null(countGetter);
    jlong *taskIDs = get_array_elements(Long, joTaskIdArray);
    uint32_t count = get_array_length(joTaskIdArray);
    int result = xl_dl_get_unfinished_tasks((uint64_t*)taskIDs, (uint32_t*)&count);

    env->SetIntField(countGetter, env->GetFieldID(env->GetObjectClass(countGetter), "value", "I"), (jint)count);
    release_array_elements(Long, joTaskIdArray, taskIDs);

    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getFinishedTask(JNIEnv *env, jclass clazz, jlongArray joTaskIdArray, jobject countGetter) {
    check_param_null(countGetter);
    jlong *taskIDs = get_array_elements(Long, joTaskIdArray);
    uint32_t count = get_array_length(joTaskIdArray);
    int result = xl_dl_get_finished_tasks((uint64_t*)taskIDs, (uint32_t*)&count);

    env->SetIntField(countGetter, env->GetFieldID(env->GetObjectClass(countGetter), "value", "I"), (jint)count);
    release_array_elements(Long, joTaskIdArray, taskIDs);

    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_createP2spTask(JNIEnv *env, jclass clazz, jobject joCreateInfo, jobject taskIdGetter) {
    check_param_null(joCreateInfo);
    check_param_null(taskIdGetter);
    jclass jcCreateInfo = env->GetObjectClass(joCreateInfo);

    auto jsSavePath = (jstring)env->GetObjectField(joCreateInfo, env->GetFieldID(jcCreateInfo, "savePath", "Ljava/lang/String;"));
    auto jsSaveName = (jstring)env->GetObjectField(joCreateInfo, env->GetFieldID(jcCreateInfo, "saveName", "Ljava/lang/String;"));
    auto jsURL = (jstring)env->GetObjectField(joCreateInfo, env->GetFieldID(jcCreateInfo, "url", "Ljava/lang/String;"));

    xl_dl_create_p2sp_info createP2spInfo;

    createP2spInfo.save_path = jstring_to_chars(jsSavePath);
    createP2spInfo.save_name = jstring_to_chars(jsSaveName);
    createP2spInfo.url = jstring_to_chars(jsURL);

    uint64_t taskId = 0;
    int result = xl_dl_create_p2sp_task(&createP2spInfo, (uint64_t*)&taskId);

    env->SetLongField(taskIdGetter, env->GetFieldID(env->GetObjectClass(taskIdGetter), "value", "J"), (jlong)taskId);
    release_jstring_chars(jsSavePath, createP2spInfo.save_path);
    release_jstring_chars(jsSaveName, createP2spInfo.save_name);
    release_jstring_chars(jsURL, createP2spInfo.url);

    env->DeleteLocalRef(jsSavePath);
    env->DeleteLocalRef(jsSaveName);
    env->DeleteLocalRef(jsURL);

    env->DeleteLocalRef(jcCreateInfo);

    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_startTask(JNIEnv *env, jclass clazz, jlong taskId) {
    return xl_dl_start_task(taskId);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_stopTask(JNIEnv *env, jclass clazz, jlong taskId) {
    return xl_dl_stop_task(taskId);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_deleteTask(JNIEnv *env, jclass clazz, jlong taskId, int deleteFile) {
    return xl_dl_delete_task(taskId, deleteFile);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getTaskState(JNIEnv *env, jclass clazz, jlong taskId, jobject joState) {
    check_param_null(joState);
    jclass jcState = env->GetObjectClass(joState);

    xl_dl_task_state taskState{};
    int result = xl_dl_get_task_state(taskId, &taskState);

    if (result == 0) {
        env->SetLongField(joState, env->GetFieldID(jcState, "speed", "J"), taskState.speed);
        env->SetIntField(joState, env->GetFieldID(jcState, "stateCode", "I"), taskState.state_code);
        env->SetLongField(joState, env->GetFieldID(jcState, "totalSize", "J"), taskState.total_size);
        env->SetLongField(joState, env->GetFieldID(jcState, "downloadedSize", "J"), taskState.downloaded_size);
        env->SetIntField(joState, env->GetFieldID(jcState, "taskErrCode", "I"), taskState.task_err_code);
        env->SetIntField(joState, env->GetFieldID(jcState, "taskTokenErr", "I"), taskState.task_token_err);
    }

    env->DeleteLocalRef(jcState);
    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getTaskInfo(JNIEnv *env, jclass clazz, jlong taskId, jstring jsInfoName, jobject infoGetter)
{
    check_param_null(infoGetter);
    const char *infoName = jstring_to_chars(jsInfoName);
    int result = XL_DL_ERROR_SUCCESS;
    do {
        if (infoName == nullptr) {
            result = XL_DL_ERROR_PARAM_ERROR;
            break;
        }

        if (strcmp(infoName, "url") == 0
            || strcmp(infoName, "save_path") == 0
            || strcmp(infoName, "save_name") == 0) {
            char info[XL_DL_MAX_SESSION_ID_LEN]{};
            uint32_t infoLen = sizeof(info);
            result = xl_dl_get_task_info(taskId, infoName, info, &infoLen);
            if (result == XL_DL_ERROR_SUCCESS) {
                json jsonInfo;
                jsonInfo[infoName] = std::string(info, infoLen);
                auto jstr = jsonInfo.dump();
                jstring jsInfo = env->NewStringUTF(jstr.c_str());
                env->SetObjectField(infoGetter, env->GetFieldID(env->GetObjectClass(infoGetter), "value", "Ljava/lang/String;"), jsInfo);
            }
        } else if (strcmp(infoName, "traffic") == 0) {
            xl_dl_task_traffic_info info{};
            uint32_t infoLen = sizeof(info);
            result = xl_dl_get_task_info(taskId, infoName, &info, &infoLen);
            json jsonInfo;
            jsonInfo["origin_size"] = info.origin_size;
            jsonInfo["p2p_size"] = info.p2p_size;
            jsonInfo["p2s_size"] = info.p2s_size;
            jsonInfo["dcdn_size"] = info.dcdn_size;
            auto jstr = jsonInfo.dump();
            jstring jsInfo = env->NewStringUTF(jstr.c_str());
            env->SetObjectField(infoGetter, env->GetFieldID(env->GetObjectClass(infoGetter), "value", "Ljava/lang/String;"), jsInfo);
        } else {
            result = XL_DL_ERROR_INFO_NAME_NOT_SUPPORT;
            break;
        }
    } while (false);

    release_jstring_chars(jsInfoName, infoName);
    return result;
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setConcurrentTaskCount(JNIEnv *env, jclass clazz, jint count) {
    return xl_dl_set_concurrent_task_count(count);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setDownloadSpeedLimit(JNIEnv *env, jclass clazz, jint speed) {
    return xl_dl_set_download_speed_limit(speed);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setUploadSwitch(JNIEnv *env, jclass clazz, jint uploadSwitch) {
    return xl_dl_set_upload_switch(uploadSwitch);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setUploadSpeedLimit(JNIEnv *env, jclass clazz, jint speed) {
    return xl_dl_set_upload_speed_limit(speed);
}

JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_version(JNIEnv *env, jclass clazz, jobject versionGetter) {
    check_param_null(versionGetter);
    char version[128]{};
    uint32_t versionLen = sizeof(version);
    int result = xl_dl_version(version, &versionLen);
    if (result == XL_DL_ERROR_SUCCESS) {
        jstring jsVersion = env->NewStringUTF(version);
        env->SetObjectField(versionGetter, env->GetFieldID(env->GetObjectClass(versionGetter), "value", "Ljava/lang/String;"), jsVersion);
    }
    return result;
}

}