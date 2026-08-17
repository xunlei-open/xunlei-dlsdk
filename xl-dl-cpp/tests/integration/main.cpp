#include <xl_dl/xl_dl_login_token.hpp>
#include <xl_dl/xl_dl_sdk.h>

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <filesystem>
#include <stdexcept>
#include <string>
#include <thread>

namespace {

const char* kAppVersion = "1.0";
const char* kDefaultTaskUrl = "https://down.sandai.net/thunder11/XunLeiWebSetup25.0.90.1592xl11.exe";

std::string env_or(const char* name, const char* fallback) {
    const char* value = std::getenv(name);
    return value && *value ? value : fallback;
}

void require(bool condition, const std::string& message) {
    if (!condition) {
        throw std::runtime_error(message);
    }
}

std::filesystem::path make_temp_dir(const std::string& prefix) {
    std::filesystem::path base = std::filesystem::temp_directory_path();
    for (int i = 0; i < 100; ++i) {
        std::filesystem::path candidate = base / (prefix + "_" + std::to_string(std::rand()));
        if (std::filesystem::create_directory(candidate)) {
            return candidate;
        }
    }
    throw std::runtime_error("failed to create temporary directory: " + prefix);
}

void remove_temp_dir(const std::filesystem::path& path) {
    if (path.empty()) {
        return;
    }
    std::error_code ignored;
    std::filesystem::remove_all(path, ignored);
}

std::string save_name_from_url(const std::string& url) {
    std::string::size_type query = url.find('?');
    std::string clean = query == std::string::npos ? url : url.substr(0, query);
    std::string::size_type slash = clean.find_last_of('/');
    std::string name = slash == std::string::npos ? clean : clean.substr(slash + 1);
    return name.empty() ? "download.tmp" : name;
}

void download_test(const std::string& task_url, const std::string& save_dir_string, int32_t timeout_seconds, uint64_t* task_id_ptr) {
    std::string save_name = save_name_from_url(task_url);
    xl_dl_create_p2sp_info create_info;
    std::memset(&create_info, 0, sizeof(create_info));
    create_info.save_path = save_dir_string.c_str();
    create_info.save_name = save_name.c_str();
    create_info.url = task_url.c_str();

    int32_t code = xl_dl_create_p2sp_task(&create_info, task_id_ptr);
    auto task_id = *task_id_ptr;
    require(code == XL_DL_ERROR_SUCCESS, "create task failed: " + std::to_string(code));
    require(task_id > 0, "task id must be greater than zero");

    code = xl_dl_start_task(task_id);
    require(code == XL_DL_ERROR_SUCCESS, "start task failed: " + std::to_string(code));

    std::chrono::steady_clock::time_point deadline =
            std::chrono::steady_clock::now() + std::chrono::seconds(timeout_seconds);
    while (std::chrono::steady_clock::now() < deadline) {
        xl_dl_task_state state;
        std::memset(&state, 0, sizeof(state));
        code = xl_dl_get_task_state(task_id, &state);
        require(code == XL_DL_ERROR_SUCCESS, "get task state failed: " + std::to_string(code));
        if (state.state_code == XL_DL_TASK_STATUS_SUCCEEDED) {
            xl_dl_delete_task(task_id, 1);
            return;
        }
        require(state.state_code != XL_DL_TASK_STATUS_FAILED, "task failed");
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    throw std::runtime_error("task " + std::to_string(task_id)
            + " did not finish within " + std::to_string(timeout_seconds) + " seconds");
}

}  // namespace

int main() {
    std::srand(static_cast<unsigned int>(
            std::chrono::high_resolution_clock::now().time_since_epoch().count()));

    uint64_t task_id = 0;
    std::filesystem::path config_dir;
    std::filesystem::path save_dir;

    try {
        std::string api_key = env_or("API_KEY", "");
        require(!api_key.empty(), "API_KEY environment variable is required");
        std::string app_id = env_or("APP_ID", "");
        require(!app_id.empty(), "APP_ID environment variable is required");

        std::string task_url = env_or("XL_DL_TEST_URL", kDefaultTaskUrl);
        int timeout_seconds = std::atoi(env_or("XL_DL_TEST_TIMEOUT_SECONDS", "900").c_str());
        config_dir = make_temp_dir("xl_dl_cpp_test_cfg");
        save_dir = make_temp_dir("xl_dl_cpp_test_downloads");

        xl_dl_init_param init_param;
        std::memset(&init_param, 0, sizeof(init_param));
        init_param.app_id = app_id.c_str();
        init_param.app_version = kAppVersion;
        const std::string config_dir_string = config_dir.string();
        const std::string save_dir_string = save_dir.string();
        init_param.cfg_path = config_dir_string.c_str();
        init_param.save_tasks = 1;

        int code = xl_dl_init(&init_param);
        require(code == XL_DL_ERROR_SUCCESS || code == XL_DL_ERROR_ALREADY_INIT, "init failed: " + std::to_string(code));

        try {
            xl_dl::login_token_result token = xl_dl::get_login_token(api_key);
            if (token.code != 0) {
                std::fprintf(stderr, "warning: get loginToken failed, code=%d, message=%s\n", token.code, token.message.c_str());
            } else if (token.token.empty()) {
                std::fprintf(stderr, "warning: loginToken is empty, continue without login\n");
            } else {
                try {
                    char session[XL_DL_MAX_SESSION_ID_LEN] = {0};
                    code = xl_dl_login(token.token.c_str(), session);
                    if (code != XL_DL_ERROR_SUCCESS) {
                        std::fprintf(stderr, "warning: login failed: %d, continue without login\n", code);
                    } else if (session[0] == '\0') {
                        std::fprintf(stderr, "warning: session id is empty, continue without login\n");
                    }
                } catch (const std::exception& error) {
                    std::fprintf(stderr, "warning: login failed: %s, continue without login\n", error.what());
                }
            }
        } catch (const std::exception& error) {
            std::fprintf(stderr, "warning: get loginToken failed: %s, continue without login\n", error.what());
        }

        download_test(task_url, save_dir_string, timeout_seconds, &task_id);
        remove_temp_dir(config_dir);
        remove_temp_dir(save_dir);

        code = xl_dl_set_dynamic_link_acceleration(false);
        require(code == XL_DL_ERROR_SUCCESS, "set dynamic link acceleration failed: " + std::to_string(code));

        download_test(task_url, save_dir_string, timeout_seconds, &task_id);

        xl_dl_uninit();
        remove_temp_dir(config_dir);
        remove_temp_dir(save_dir);

        return 0;
    } catch (const std::exception& error) {
        std::fprintf(stderr, "%s\n", error.what());
        if (task_id != 0) {
            xl_dl_delete_task(task_id, 1);
        }
        xl_dl_uninit();
        remove_temp_dir(config_dir);
        remove_temp_dir(save_dir);
        return 1;
    }
}
