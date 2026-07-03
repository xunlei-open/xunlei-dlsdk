#include <xl_dl/xl_dl_sdk.h>
#include <xl_dl/xl_dl_login_token.hpp>

#include <chrono>
#include <cerrno>
#include <cstdio>
#include <cstring>
#include <string>
#include <thread>

#ifdef _WIN32
#include <direct.h>
#define XL_DL_MKDIR(path) _mkdir(path)
#else
#include <sys/stat.h>
#include <sys/types.h>
#define XL_DL_MKDIR(path) mkdir(path, 0755)
#endif

namespace {

bool ensure_dir(const char* path) {
    if (XL_DL_MKDIR(path) == 0) {
        return true;
    }
    return errno == EEXIST;
}

}  // namespace

int download_test(const std::string& task_url, const std::string& save_name, const std::string& file_save_dir) {
    xl_dl_create_p2sp_info create_info;
    std::memset(&create_info, 0, sizeof(create_info));
    create_info.save_path = file_save_dir.c_str();
    create_info.save_name = save_name.c_str();
    create_info.url = task_url.c_str();

    uint64_t task_id = 0;
    auto code = xl_dl_create_p2sp_task(&create_info, &task_id);
    std::printf("xl_dl_create_p2sp_task result:%d task_id:%llu\n", code, static_cast<unsigned long long>(task_id));
    if (code != XL_DL_ERROR_SUCCESS) {
        return code;
    }

    code = xl_dl_start_task(task_id);
    std::printf("xl_dl_start_task result:%d\n", code);
    if (code != XL_DL_ERROR_SUCCESS) {
        return code;
    }

    while (true) {
        xl_dl_task_state state;
        std::memset(&state, 0, sizeof(state));
        code = xl_dl_get_task_state(task_id, &state);
        std::printf("\rstate:%u downloaded:%llu/%llu speed:%llu",
                state.state_code,
                static_cast<unsigned long long>(state.downloaded_size),
                static_cast<unsigned long long>(state.total_size),
                static_cast<unsigned long long>(state.speed));
        std::fflush(stdout);
        if (code != XL_DL_ERROR_SUCCESS
                || state.state_code == XL_DL_TASK_STATUS_SUCCEEDED
                || state.state_code == XL_DL_TASK_STATUS_FAILED) {
            std::printf("\n");
            break;
        }
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    return code;
}

int main() {
    const std::string api_key = "xl_ba3edc87e2734c8bf177a04f3dd4xxx";  // TODO: Replace with your own API key.
    const std::string task_url = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe";
    const std::string save_name = "XunLeiSetup12.0.12.2510.exe";

    char version[128] = {0};
    uint32_t version_len = sizeof(version);
    int code = xl_dl_version(version, &version_len);
    std::printf("xl_dl_version result:%d version:%s\n", code, version);

    std::string SDK_CONFIG_DIR = "/tmp/xl_dl_sdk_conf";
    std::string FILE_SAVE_DIR = "/tmp/ThunderDownload";
    if (!ensure_dir(SDK_CONFIG_DIR.c_str())) {
        std::printf("create config dir failed: %s\n", SDK_CONFIG_DIR.c_str());
        return 1;
    }
    if (!ensure_dir(FILE_SAVE_DIR.c_str())) {
        std::printf("create save dir failed: %s\n", FILE_SAVE_DIR.c_str());
        return 1;
    }

    xl_dl_init_param init_param;
    std::memset(&init_param, 0, sizeof(init_param));
    init_param.app_id = "eGwtcVo4SDEwMDMwAAAAAy4nxxx=";  // TODO: Replace with your own app ID.
    init_param.app_version = "1.0";
    init_param.cfg_path = SDK_CONFIG_DIR.c_str();
    init_param.save_tasks = 1;

    code = xl_dl_init(&init_param);
    std::printf("xl_dl_init result:%d cfg_path:%s\n", code, SDK_CONFIG_DIR.c_str());
    if (code != XL_DL_ERROR_SUCCESS && code != XL_DL_ERROR_ALREADY_INIT) {
        return code;
    }

    xl_dl::login_token_result token = xl_dl::get_login_token(api_key);
    if (token.code != 0 || token.token.empty()) {
        std::printf("get login token failed, code:%d message:%s\n", token.code, token.message.c_str());
        xl_dl_uninit();
        return 1;
    }

    char session[XL_DL_MAX_SESSION_ID_LEN] = {0};
    code = xl_dl_login(token.token.c_str(), session);
    std::printf("xl_dl_login result:%d session:%s\n", code, session);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    code = download_test(task_url, save_name, FILE_SAVE_DIR);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    code = _xl_dl_set_download_url_acceleration(false);
    std::printf("xl_dl_set_download_url_acceleration disable result:%d\n", code);
    code = download_test(task_url, save_name, FILE_SAVE_DIR);
    if (code != XL_DL_ERROR_SUCCESS) {
        xl_dl_uninit();
        return code;
    }

    int uninit_code = xl_dl_uninit();
    std::printf("xl_dl_uninit result:%d\n", uninit_code);
    return code == XL_DL_ERROR_SUCCESS || code == XL_DL_ERROR_ALREADY_INIT ? 0 : code;
}
